package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.SubnetBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final List<RegistryEntry<Item>> DUMMY_ITEMS;
    public static final Map<Voltage, RegistryEntry<Item>> ELECTRIC_MOTOR;
    public static final Map<Voltage, RegistryEntry<Item>> ELECTRIC_PUMP;
    public static final Map<Voltage, RegistryEntry<Item>> ELECTRIC_PISTON;
    public static final Map<Voltage, RegistryEntry<Item>> CONVEYOR_MODULE;
    public static final Map<Voltage, RegistryEntry<Item>> ROBOT_ARM;
    public static final Map<Voltage, RegistryEntry<Item>> SENSOR;
    public static final Map<Voltage, RegistryEntry<Item>> FIELD_GENERATOR;
    public static final Map<Voltage, RegistryEntry<Item>> MACHINE_HULL;
    public static final Map<Voltage, RegistryEntry<Item>> RESEARCH_EQUIPMENT;
    public static final Map<Voltage, RegistryEntry<BatteryItem>> BATTERY;
    public static final Map<Voltage, RegistryEntry<CableBlock>> CABLE;
    public static final RegistryEntry<Item> VACUUM_TUBE;
    public static final RegistryEntry<SimpleFluid> STEAM;
    public static final RegistryEntry<Block> HEAT_PROOF_BLOCK;
    public static final RegistryEntry<SubnetBlock> TEST_TRANSFORMER;

    static {
        DUMMY_ITEMS = new ArrayList<>();

        VACUUM_TUBE = REGISTRATE.item("circuit/vacuum_tube", Item::new).register();

        STEAM = REGISTRATE.simpleFluid("steam", gregtech("blocks/fluids/fluid.steam"));

        HEAT_PROOF_BLOCK = REGISTRATE.block("multi_block/solid/heat_proof", Block::new)
                .properties($ -> $.strength(2f, 8f))
                .register();

        ELECTRIC_MOTOR = dummyItem("electric_motor");
        ELECTRIC_PUMP = dummyItem("electric_pump");
        ELECTRIC_PISTON = dummyItem("electric_piston");
        CONVEYOR_MODULE = dummyItem("conveyor_module");
        ROBOT_ARM = dummyItem("robot_arm");
        SENSOR = dummyItem("sensor");
        FIELD_GENERATOR = dummyItem("field_generator");
        MACHINE_HULL = dummyBuilder("machine_hull")
                .voltages(Voltage.ULV, Voltage.IV)
                .buildObject();

        RESEARCH_EQUIPMENT = ComponentBuilder.<Item, MaterialSet>builder((v, mat) -> REGISTRATE
                        .item("component/" + v.id + "/research_equipment", Item::new)
                        .tint(0xFFFFFFFF, mat.color)
                        .register())
                .voltage(Voltage.ULV, IRON)
                .voltage(Voltage.LV, STEEL)
                .voltage(Voltage.MV, ALUMINIUM)
                // TODO
                .voltage(Voltage.HV, IRON)
                // TODO
                .voltage(Voltage.EV, IRON)
                .buildObject();

        BATTERY = ComponentBuilder.dummy(v -> REGISTRATE
                        .item("network/" + v.id + "/battery", prop ->
                                new BatteryItem(prop, 12000 * v.value))
                        .register())
                .voltages(Voltage.LV, Voltage.HV)
                .buildObject();

        CABLE = ComponentBuilder.<CableBlock, MaterialSet>builder((v, mat) -> REGISTRATE
                        .block("network/" + v.id + "/cable", CableBlock.cable(v))
                        .transform(CableBlock.tint(v, mat.color))
                        .translucent()
                        .register())
                .voltage(Voltage.ULV, IRON)
                .voltage(Voltage.LV, TIN)
                .voltage(Voltage.MV, COPPER)
                .voltage(Voltage.HV, GOLD)
                .voltage(Voltage.EV, ALUMINIUM)
                .buildObject();

        TEST_TRANSFORMER = REGISTRATE.block("network/transformer",
                        prop -> new SubnetBlock(prop, Voltage.LV, Voltage.ULV))
                .translucent()
                .register();
    }

    public static void init() {}

    private static ComponentBuilder.DummyBuilder<Item, ?> dummyBuilder(String name) {
        return ComponentBuilder.dummy(v -> REGISTRATE
                .item("component/" + v.id + "/" + name, Item::new)
                .register());
    }

    private static Map<Voltage, RegistryEntry<Item>> dummyItem(String name) {
        var ret = dummyBuilder(name)
                .voltages(Voltage.LV, Voltage.IV)
                .buildObject();
        DUMMY_ITEMS.addAll(ret.values());
        return ret;
    }
}

