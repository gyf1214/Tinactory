package org.shsts.tinactory.content.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.SoundType;
import org.shsts.tinactory.content.logistics.MENetworkBridge;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.network.BridgeBlock;
import org.shsts.tinactory.content.network.SubnetBlock;
import org.shsts.tinactory.content.tool.PoweredChainsawItem;
import org.shsts.tinactory.content.tool.PoweredDrillItem;
import org.shsts.tinactory.content.tool.PoweredItem;
import org.shsts.tinactory.content.tool.PoweredToolConfig;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.integration.builder.BlockEntityBuilder;
import org.shsts.tinactory.integration.common.CellItem;
import org.shsts.tinactory.integration.material.MaterialSet;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER_ITEM;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllItems.COMPONENTS;
import static org.shsts.tinactory.AllMaterials.getMaterial;
import static org.shsts.tinactory.AllRegistries.ITEMS;
import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentMeta extends MetaConsumer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ComponentMeta() {
        super("Component");
    }

    public record VoltageWithConfig(Voltage voltage, JsonObject jo) {}

    public static Collection<VoltageWithConfig> parseVoltageConfig(JsonObject jo, String field) {
        if (!jo.has(field)) {
            throw new JsonSyntaxException("Missing field " + field);
        }
        var je = jo.get(field);
        if (je.isJsonObject()) {
            var ret = new ArrayList<VoltageWithConfig>();
            for (var entry : je.getAsJsonObject().entrySet()) {
                // add base settings
                var jo2 = new JsonObject();
                for (var entry1 : jo.entrySet()) {
                    if (!entry1.getKey().equals(field)) {
                        jo2.add(entry1.getKey(), entry1.getValue().deepCopy());
                    }
                }

                // add specific setting
                var v = Voltage.fromName(entry.getKey());
                var jo1 = GsonHelper.convertToJsonObject(entry.getValue(), field);
                for (var entry1 : jo1.entrySet()) {
                    jo2.add(entry1.getKey(), entry1.getValue().deepCopy());
                }
                ret.add(new VoltageWithConfig(v, jo2));
            }
            return ret;
        } else if (je.isJsonArray()) {
            var ret = new ArrayList<VoltageWithConfig>();
            for (var je1 : je.getAsJsonArray()) {
                var v = Voltage.fromName(GsonHelper.convertToString(je1, field));
                ret.add(new VoltageWithConfig(v, jo));
            }
            return ret;
        } else if (je.isJsonPrimitive()) {
            var str = GsonHelper.convertToString(je, field);
            if (str.contains("-")) {
                var fields = str.split("-");
                return Voltage.between(Voltage.fromName(fields[0]), Voltage.fromName(fields[1]))
                    .stream().map(v -> new VoltageWithConfig(v, jo))
                    .toList();
            } else {
                return List.of(new VoltageWithConfig(Voltage.fromName(str), jo));
            }
        }
        throw new JsonSyntaxException("Cannot parse voltages from field " + field);
    }

    public static Collection<Voltage> parseVoltage(JsonObject jo, String field) {
        return parseVoltageConfig(jo, field).stream().map(VoltageWithConfig::voltage).toList();
    }

    private void buildComponents(String name, JsonObject jo) {
        var tint = GsonHelper.getAsInt(jo, "voltageTint", -1);
        var components = new HashMap<Voltage, IEntry<Item>>();
        for (var v : parseVoltage(jo, "items")) {
            var id = "component/" + v.id + "/" + name;
            var builder = REGISTRATE.item(id);
            if (tint >= 0) {
                builder.tint(() -> () -> ($, i) -> i == tint ? v.color : 0xFFFFFFFF);
            }
            builder.creativeTab(CreativeModeTabs.INGREDIENTS);
            var item = builder.register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    private void buildBatteries(String name, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "items");
        var components = new HashMap<Voltage, IEntry<PoweredItem>>();
        for (var entry : jo1.entrySet()) {
            var v = Voltage.fromName(entry.getKey());
            var capacity = GsonHelper.convertToInt(entry.getValue(), "items");
            var id = "network/" + v.id + "/" + name;
            var item = REGISTRATE.item(id, prop -> new PoweredItem(prop, v, capacity))
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, PoweredItem::fullItem)
                .itemProperty(PoweredItem.ITEM_PROPERTY, () -> () -> (stack, $1, $2, $3) ->
                    PoweredItem.normalizedPower(stack))
                .register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    private void buildPoweredDrills(String name, JsonObject jo) {
        var components = new HashMap<Voltage, IEntry<PoweredDrillItem>>();
        for (var entry : parseVoltageConfig(jo, "items")) {
            var v = entry.voltage();
            var jo1 = entry.jo();
            var areaMiningRadius = getNonNegativeInt(jo1, "areaMiningRadius");
            var config = poweredToolConfig(entry, areaMiningRadius);
            var id = "tool/" + v.id + "/" + name;
            var item = REGISTRATE.item(id, prop ->
                    new PoweredDrillItem(prop, config, areaMiningRadius))
                .tint(() -> () -> (stack, layer) -> layer == 1 ? config.material().color : 0xFFFFFFFF)
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, PoweredItem::fullItem)
                .register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    private void buildPoweredChainsaws(String name, JsonObject jo) {
        var components = new HashMap<Voltage, IEntry<PoweredChainsawItem>>();
        for (var entry : parseVoltageConfig(jo, "items")) {
            var v = entry.voltage();
            var jo1 = entry.jo();
            var maxSearchBlocks = getNonNegativeInt(jo1, "maxSearchBlocks");
            if (maxSearchBlocks == 1) {
                throw new JsonSyntaxException("maxSearchBlocks must be zero or at least two");
            }
            var config = poweredToolConfig(entry, maxSearchBlocks);
            var id = "tool/" + v.id + "/" + name;
            var item = REGISTRATE.item(id, prop ->
                    new PoweredChainsawItem(prop, config, maxSearchBlocks))
                .tint(() -> () -> (stack, layer) -> layer == 1 ? config.material().color : 0xFFFFFFFF)
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, PoweredItem::fullItem)
                .register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    private PoweredToolConfig poweredToolConfig(VoltageWithConfig entry, int abilityLimit) {
        var jo = entry.jo();
        var capacity = getIntegralLong(jo, "capacity");
        var normalUseCost = getIntegralLong(jo, "normalUseCost");
        var specialAbilityCost = abilityLimit == 0 ? 0 : getIntegralLong(jo, "specialAbilityCost");
        if (capacity <= 0) {
            throw new JsonSyntaxException("capacity must be positive");
        }
        if (normalUseCost <= 0 || normalUseCost > capacity) {
            throw new JsonSyntaxException("normalUseCost must be positive and no greater than capacity");
        }
        if (abilityLimit > 0 && (specialAbilityCost <= 0 || specialAbilityCost > capacity)) {
            throw new JsonSyntaxException("specialAbilityCost must be positive and no greater than capacity");
        }

        var miningSpeed = GsonHelper.getAsDouble(jo, "miningSpeed");
        if (!Double.isFinite(miningSpeed) || miningSpeed <= 0 || miningSpeed > Float.MAX_VALUE) {
            throw new JsonSyntaxException("miningSpeed must be a positive finite number");
        }
        var tierName = GsonHelper.getAsString(jo, "harvestTier").toUpperCase(Locale.ROOT);
        Tier harvestTier;
        try {
            harvestTier = Tiers.valueOf(tierName);
        } catch (IllegalArgumentException e) {
            throw new JsonSyntaxException("Unknown harvestTier " + tierName, e);
        }
        var materialName = GsonHelper.getAsString(jo, "material");
        MaterialSet material = getMaterial(materialName);
        if (material == null) {
            throw new JsonSyntaxException("Unknown material " + materialName);
        }
        return new PoweredToolConfig(entry.voltage(), capacity, normalUseCost, specialAbilityCost,
            (float) miningSpeed, harvestTier, material);
    }

    private static int getNonNegativeInt(JsonObject jo, String field) {
        var value = getIntegralLong(jo, field);
        if (value < 0 || value > Integer.MAX_VALUE) {
            throw new JsonSyntaxException(field + " must be a nonnegative integer");
        }
        return (int) value;
    }

    private static long getIntegralLong(JsonObject jo, String field) {
        var value = jo.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonSyntaxException("Expected integer field " + field);
        }
        try {
            return value.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new JsonSyntaxException(field + " must be an integer in long range", e);
        }
    }

    private void buildCables(String name, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "items");
        var components = new HashMap<Voltage, IEntry<CableBlock>>();
        for (var entry : jo1.entrySet()) {
            var v = Voltage.fromName(entry.getKey());
            var jo2 = GsonHelper.convertToJsonObject(entry.getValue(), "items");
            var resistance = GsonHelper.getAsDouble(jo2, "resistance");
            var mat = getMaterial(GsonHelper.getAsString(jo2, "material"));
            var bare = GsonHelper.getAsBoolean(jo2, "bare", false);
            var id = "network/" + v.id + "/" + name;

            var block = REGISTRATE.block(id, CableBlock.cable(v, resistance, mat, bare))
                .properties($ -> $.strength(2f).sound(bare ? SoundType.METAL : SoundType.WOOL))
                .creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .transform(CableBlock.tint(mat.color, bare))
                .register();
            components.put(v, block);
        }
        COMPONENTS.put(name, components);
    }

    private void buildSubnets(String name, JsonObject jo) {
        var components = new HashMap<Voltage, IEntry<SubnetBlock>>();
        var voltageOffset = GsonHelper.getAsInt(jo, "voltageOffset");
        for (var v : parseVoltage(jo, "items")) {
            var id = "network/" + v.id + "/" + name;
            var v1 = Voltage.fromRank(v.rank + voltageOffset);
            var block = REGISTRATE.block(id, SubnetBlock.factory(v, v1))
                .properties(MACHINE_PROPERTY)
                .creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .tint(i -> switch (i) {
                    case 1 -> v.color;
                    case 2 -> v1.color;
                    default -> 0xFFFFFFFF;
                }).register();
            components.put(v, block);
        }
        COMPONENTS.put(name, components);
    }

    private void buildNetworkBridge(String name, JsonObject jo) {
        var components = new HashMap<Voltage, IEntry<BridgeBlock>>();
        for (var entry : parseVoltageConfig(jo, "items")) {
            var v = entry.voltage();
            var jo1 = entry.jo();
            var id = "network/" + v.id + "/" + name;
            var power = jo1.has("power") ? GsonHelper.getAsDouble(jo1, "power") :
                v.value * GsonHelper.getAsDouble(jo1, "amperage");
            var block = BlockEntityBuilder.builder(id, BridgeBlock.factory(v, tooltip ->
                    addTooltip(tooltip, "machinePower", NUMBER_FORMAT.format(power))))
                .transform(MachineSet.baseMachine(false))
                .blockEntity()
                .capability(MACHINE, ELECTRIC_MACHINE)
                .transform(MENetworkBridge.factory(power))
                .end()
                .block()
                .creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .end()
                .buildObject();
            components.put(v, block);
        }
        COMPONENTS.put(name, components);
    }

    private void buildFluidCells(String name, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "items");
        var components = new HashMap<Voltage, IEntry<CellItem>>();
        for (var entry : jo1.entrySet()) {
            var v = Voltage.fromName(entry.getKey());
            var jo2 = GsonHelper.convertToJsonObject(entry.getValue(), "items");
            var mat = getMaterial(GsonHelper.getAsString(jo2, "material"));
            var capacity = GsonHelper.getAsInt(jo2, "capacity");
            var id = "tool/" + name + "/" + mat.name;

            var item = REGISTRATE.item(id, CellItem.factory(capacity))
                .creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .capability(FLUID_HANDLER_ITEM)
                .tint(() -> () -> CellItem::getTint)
                .register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    private void buildSet(String name, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "items");
        var components = new HashMap<Voltage, IEntry<Item>>();
        for (var entry : jo1.entrySet()) {
            var v = Voltage.fromName(entry.getKey());
            var loc = ResourceLocation.parse(GsonHelper.convertToString(entry.getValue(), "items"));
            var item = ITEMS.getEntry(loc);
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var type = GsonHelper.getAsString(jo, "type", "default");
        var name = loc.getPath();
        switch (type) {
            case "default" -> buildComponents(name, jo);
            case "battery" -> buildBatteries(name, jo);
            case "powered_drill" -> buildPoweredDrills(name, jo);
            case "powered_chainsaw" -> buildPoweredChainsaws(name, jo);
            case "cable" -> buildCables(name, jo);
            case "subnet" -> buildSubnets(name, jo);
            case "network_bridge" -> buildNetworkBridge(name, jo);
            case "fluid_cell" -> buildFluidCells(name, jo);
            case "set" -> buildSet(name, jo);
            default -> LOGGER.debug("Skip unknown type: {}", type);
        }
    }
}
