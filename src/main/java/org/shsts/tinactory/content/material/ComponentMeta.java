package org.shsts.tinactory.content.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.SubnetBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.CellItem;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllItems.COMPONENTS;
import static org.shsts.tinactory.content.AllMaterials.getMaterial;
import static org.shsts.tinactory.content.AllRegistries.ITEMS;
import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;

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
            var item = builder.register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
    }

    private void buildBatteries(String name, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "items");
        var components = new HashMap<Voltage, IEntry<BatteryItem>>();
        for (var entry : jo1.entrySet()) {
            var v = Voltage.fromName(entry.getKey());
            var capacity = GsonHelper.convertToInt(entry.getValue(), "items");
            var id = "network/" + v.id + "/" + name;
            var item = REGISTRATE.item(id, prop -> new BatteryItem(prop, v, capacity)).register();
            components.put(v, item);
        }
        COMPONENTS.put(name, components);
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
                .material(Material.HEAVY_METAL)
                .properties($ -> $.strength(2f).sound(bare ? SoundType.METAL : SoundType.WOOL))
                .transform(CableBlock.tint(mat.color, bare))
                .translucent()
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
                .material(Material.HEAVY_METAL)
                .properties(MACHINE_PROPERTY)
                .translucent()
                .tint(i -> switch (i) {
                    case 0 -> v.color;
                    case 1 -> v1.color;
                    default -> 0xFFFFFFFF;
                }).register();
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
            var loc = new ResourceLocation(GsonHelper.convertToString(entry.getValue(), "items"));
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
            case "cable" -> buildCables(name, jo);
            case "subnet" -> buildSubnets(name, jo);
            case "fluid_cell" -> buildFluidCells(name, jo);
            case "set" -> buildSet(name, jo);
            default -> LOGGER.debug("Skip unknown type: {}", type);
        }
    }
}
