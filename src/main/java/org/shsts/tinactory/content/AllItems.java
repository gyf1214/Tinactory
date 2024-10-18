package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.electric.CircuitLevel;
import org.shsts.tinactory.content.electric.CircuitTier;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.content.material.RubberTreeGrower;
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
    public static final Map<Voltage, RegistryEntry<SubnetBlock>> TRANSFORMER;

    // circuits
    public static final Circuits.Circuit VACUUM_TUBE;
    public static final Circuits.Circuit ELECTRONIC_CIRCUIT;
    public static final Circuits.Circuit GOOD_ELECTRONIC;
    public static final Circuits.Circuit BASIC_INTEGRATED;
    public static final Circuits.Circuit GOOD_INTEGRATED;
    public static final Circuits.Circuit ADVANCED_INTEGRATED;

    // circuit components
    public static final Circuits.CircuitComponent RESISTOR;
    public static final Circuits.CircuitComponent CAPACITOR;
    public static final Circuits.CircuitComponent INDUCTOR;
    public static final Circuits.CircuitComponent DIODE;
    public static final Circuits.CircuitComponent TRANSISTOR;

    public static final RegistryEntry<Item> STICKY_RESIN;
    public static final RegistryEntry<SimpleFluid> STEAM;
    public static final RegistryEntry<Block> HEAT_PROOF_BLOCK;
    public static final RegistryEntry<Block> CUPRONICKEL_COIL_BLOCK;
    public static final RegistryEntry<RubberLogBlock> RUBBER_LOG;
    public static final RegistryEntry<LeavesBlock> RUBBER_LEAVES;
    public static final RegistryEntry<SaplingBlock> RUBBER_SAPLING;

    static {
        DUMMY_ITEMS = new ArrayList<>();

        VACUUM_TUBE = Circuits.circuit(CircuitTier.ELECTRONIC, CircuitLevel.NORMAL, "vacuum_tube");
        ELECTRONIC_CIRCUIT = Circuits.circuit(CircuitTier.ELECTRONIC, CircuitLevel.ASSEMBLY, "electronic");
        GOOD_ELECTRONIC = Circuits.circuit(CircuitTier.ELECTRONIC, CircuitLevel.WORKSTATION, "good_electronic");
        BASIC_INTEGRATED = Circuits.circuit(CircuitTier.INTEGRATED, CircuitLevel.NORMAL, "basic_integrated");
        GOOD_INTEGRATED = Circuits.circuit(CircuitTier.INTEGRATED, CircuitLevel.ASSEMBLY, "good_integrated");
        ADVANCED_INTEGRATED = Circuits.circuit(CircuitTier.INTEGRATED, CircuitLevel.WORKSTATION, "advanced_integrated");

        RESISTOR = Circuits.component("resistor");
        CAPACITOR = Circuits.component("capacitor");
        INDUCTOR = Circuits.component("inductor");
        DIODE = Circuits.component("diode");
        TRANSISTOR = Circuits.component("transistor");

        Circuits.addBoards();

        STICKY_RESIN = REGISTRATE.item("rubber_tree/sticky_resin", Item::new).register();

        RUBBER_LOG = REGISTRATE.block("rubber_tree/log", RubberLogBlock::new)
                .material(Material.WOOD)
                .properties(p -> p.sound(SoundType.WOOD))
                .register();

        RUBBER_LEAVES = REGISTRATE.block("rubber_tree/leaves", LeavesBlock::new)
                .material(Material.LEAVES)
                .properties(p -> p.strength(0.2f).randomTicks()
                        .sound(SoundType.GRASS).noOcclusion()
                        .isValidSpawn(($1, $2, $3, $4) -> false)
                        .isSuffocating(($1, $2, $3) -> false)
                        .isViewBlocking(($1, $2, $3) -> false))
                .renderType(() -> RenderType::cutout)
                .tint(0xFF55FF55)
                .register();

        RUBBER_SAPLING = REGISTRATE.block("rubber_tree/sapling",
                        prop -> new SaplingBlock(new RubberTreeGrower(), prop))
                .material(Material.PLANT)
                .properties(p -> p.noCollission().randomTicks()
                        .instabreak().sound(SoundType.GRASS))
                .renderType(() -> RenderType::cutout)
                .register();

        STEAM = REGISTRATE.simpleFluid("steam", gregtech("blocks/fluids/fluid.steam"));

        HEAT_PROOF_BLOCK = REGISTRATE.block("multi_block/solid/heat_proof", Block::new)
                .properties($ -> $.strength(2f, 8f))
                .register();

        CUPRONICKEL_COIL_BLOCK = REGISTRATE.block("multi_block/coil/cupronickel", Block::new)
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
                                new BatteryItem(prop, v, 12000 * v.value))
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

        TRANSFORMER = ComponentBuilder.dummy(v -> REGISTRATE
                        .block("network/" + v.id + "/transformer", SubnetBlock.transformer(v))
                        .translucent().register())
                .voltages(Voltage.LV, Voltage.IV)
                .buildObject();
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

