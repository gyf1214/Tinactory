package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.electric.CircuitLevel;
import org.shsts.tinactory.content.electric.CircuitTier;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.logistics.MEStorageCell;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.content.material.RubberTreeGrower;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.SubnetBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.CellItem;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM_STEEL;
import static org.shsts.tinactory.content.electric.Circuits.circuit;
import static org.shsts.tinactory.content.electric.Circuits.circuitComponent;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final Set<IEntry<Item>> COMPONENT_ITEMS;
    public static final Map<Voltage, IEntry<Item>> ELECTRIC_MOTOR;
    public static final Map<Voltage, IEntry<Item>> ELECTRIC_PUMP;
    public static final Map<Voltage, IEntry<Item>> ELECTRIC_PISTON;
    public static final Map<Voltage, IEntry<Item>> CONVEYOR_MODULE;
    public static final Map<Voltage, IEntry<Item>> ROBOT_ARM;
    public static final Map<Voltage, IEntry<Item>> SENSOR;
    public static final Map<Voltage, IEntry<Item>> EMITTER;
    public static final Map<Voltage, IEntry<Item>> FIELD_GENERATOR;
    public static final Map<Voltage, IEntry<Item>> MACHINE_HULL;
    public static final Map<Voltage, IEntry<Item>> RESEARCH_EQUIPMENT;
    public static final Map<Voltage, IEntry<BatteryItem>> BATTERY;
    public static final Map<Voltage, IEntry<CableBlock>> CABLE;
    public static final Map<Voltage, IEntry<SubnetBlock>> TRANSFORMER;
    public static final Map<Voltage, IEntry<SubnetBlock>> ELECTRIC_BUFFER;
    public static final Map<Voltage, Supplier<? extends ItemLike>> GRINDER;
    public static final Map<Voltage, Supplier<? extends ItemLike>> BUZZSAW;

    // circuits
    public static final Circuits.Circuit VACUUM_TUBE;
    public static final Circuits.Circuit ELECTRONIC_CIRCUIT;
    public static final Circuits.Circuit GOOD_ELECTRONIC;
    public static final Circuits.Circuit BASIC_INTEGRATED;
    public static final Circuits.Circuit GOOD_INTEGRATED;
    public static final Circuits.Circuit ADVANCED_INTEGRATED;
    public static final Circuits.Circuit NAND_CHIP;
    public static final Circuits.Circuit MICROPROCESSOR;
    public static final Circuits.Circuit INTEGRATED_PROCESSOR;
    public static final Circuits.Circuit PROCESSOR_ASSEMBLY;
    public static final Circuits.Circuit WORKSTATION;
    public static final Circuits.Circuit MAINFRAME;

    // circuit components
    public static final Circuits.CircuitComponent RESISTOR;
    public static final Circuits.CircuitComponent CAPACITOR;
    public static final Circuits.CircuitComponent INDUCTOR;
    public static final Circuits.CircuitComponent DIODE;
    public static final Circuits.CircuitComponent TRANSISTOR;

    // chips
    public static final List<IEntry<Item>> BOULES;
    public static final List<IEntry<Item>> RAW_WAFERS;
    public static final Map<String, IEntry<Item>> WAFERS;
    public static final Map<String, IEntry<Item>> CHIPS;

    public static final IEntry<Item> STICKY_RESIN;
    public static final IEntry<RubberLogBlock> RUBBER_LOG;
    public static final IEntry<LeavesBlock> RUBBER_LEAVES;
    public static final IEntry<SaplingBlock> RUBBER_SAPLING;
    public static final IEntry<Item> GOOD_GRINDER;
    public static final IEntry<Item> ADVANCED_GRINDER;
    public static final IEntry<Item> BASIC_BUZZSAW;
    public static final IEntry<Item> GOOD_BUZZSAW;
    public static final IEntry<Item> ADVANCED_BUZZSAW;
    public static final Map<Voltage, IEntry<CellItem>> FLUID_CELL;
    public static final IEntry<Item> ITEM_FILTER;
    public static final IEntry<Item> FERTILIZER;
    public static final IEntry<MEStorageCell> ME_STORAGE_CELL;

    static {
        COMPONENT_ITEMS = new HashSet<>();

        VACUUM_TUBE = circuit(CircuitTier.ELECTRONIC, CircuitLevel.NORMAL, "vacuum_tube");
        ELECTRONIC_CIRCUIT = circuit(CircuitTier.ELECTRONIC, CircuitLevel.ASSEMBLY, "electronic");
        GOOD_ELECTRONIC = circuit(CircuitTier.ELECTRONIC, CircuitLevel.WORKSTATION, "good_electronic");
        BASIC_INTEGRATED = circuit(CircuitTier.INTEGRATED, CircuitLevel.NORMAL, "basic_integrated");
        GOOD_INTEGRATED = circuit(CircuitTier.INTEGRATED, CircuitLevel.ASSEMBLY, "good_integrated");
        ADVANCED_INTEGRATED = circuit(CircuitTier.INTEGRATED, CircuitLevel.WORKSTATION, "advanced_integrated");
        NAND_CHIP = circuit(CircuitTier.CPU, CircuitLevel.MINI, "nand_chip");
        MICROPROCESSOR = circuit(CircuitTier.CPU, CircuitLevel.MICRO, "microprocessor");
        INTEGRATED_PROCESSOR = circuit(CircuitTier.CPU, CircuitLevel.NORMAL, "processor");
        PROCESSOR_ASSEMBLY = circuit(CircuitTier.CPU, CircuitLevel.ASSEMBLY, "assembly");
        WORKSTATION = circuit(CircuitTier.CPU, CircuitLevel.WORKSTATION, "workstation");
        MAINFRAME = circuit(CircuitTier.CPU, CircuitLevel.MAINFRAME, "mainframe");

        RESISTOR = circuitComponent("resistor");
        CAPACITOR = circuitComponent("capacitor");
        INDUCTOR = circuitComponent("inductor");
        DIODE = circuitComponent("diode");
        TRANSISTOR = circuitComponent("transistor");

        Circuits.addBoards();

        STICKY_RESIN = simple("rubber_tree/sticky_resin");

        RUBBER_LOG = REGISTRATE.block("rubber_tree/log", RubberLogBlock::new)
            .material(Material.WOOD)
            .properties(p -> p.sound(SoundType.WOOD))
            .register();

        RUBBER_LEAVES = REGISTRATE.block("rubber_tree/leaves", LeavesBlock::new)
            .material(Material.LEAVES)
            .properties(p -> p.strength(0.2f).randomTicks()
                .sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn(AllItems::never)
                .isSuffocating(AllItems::never)
                .isViewBlocking(AllItems::never))
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

        ELECTRIC_MOTOR = component("electric_motor");
        ELECTRIC_PUMP = component("electric_pump");
        ELECTRIC_PISTON = component("electric_piston");
        CONVEYOR_MODULE = component("conveyor_module");
        ROBOT_ARM = component("robot_arm");
        SENSOR = component("sensor");
        EMITTER = component("emitter");
        FIELD_GENERATOR = component("field_generator");
        MACHINE_HULL = componentBuilder("machine_hull")
            .voltages(Voltage.ULV, Voltage.IV)
            .buildObject();

        RESEARCH_EQUIPMENT = ComponentBuilder.simple(v -> REGISTRATE
                .item("component/" + v.id + "/research_equipment", Item::new)
                .tint(0xFFFFFFFF, v.color)
                .register())
            .voltages(Voltage.ULV, Voltage.EV)
            .buildObject();

        BATTERY = ComponentBuilder.simple(v -> REGISTRATE
                .item("network/" + v.id + "/battery", prop ->
                    new BatteryItem(prop, v, 12000 * v.value))
                .register())
            .voltages(Voltage.LV, Voltage.HV)
            .buildObject();

        CABLE = ComponentBuilder.<CableBlock, MaterialSet>builder((v, mat) -> REGISTRATE
                .block("network/" + v.id + "/cable", CableBlock.cable(v, mat))
                .transform(CableBlock.tint(v, mat.color))
                .translucent()
                .register())
            .voltage(Voltage.ULV, IRON)
            .voltage(Voltage.LV, TIN)
            .voltage(Voltage.MV, COPPER)
            .voltage(Voltage.HV, GOLD)
            .voltage(Voltage.EV, ALUMINIUM)
            .buildObject();

        TRANSFORMER = ComponentBuilder.simple(v -> REGISTRATE
                .block("network/" + v.id + "/transformer", SubnetBlock.transformer(v))
                .translucent()
                .tint(i -> switch (i) {
                    case 0 -> v.color;
                    case 1 -> Voltage.fromRank(v.rank - 1).color;
                    default -> 0xFFFFFFFF;
                }).register())
            .voltages(Voltage.LV, Voltage.IV)
            .buildObject();

        ELECTRIC_BUFFER = ComponentBuilder.simple(v -> REGISTRATE
                .block("network/" + v.id + "/electric_buffer", SubnetBlock.buffer(v))
                .translucent()
                .tint(i -> i < 2 ? v.color : 0xFFFFFFFF)
                .register())
            .voltages(Voltage.ULV, Voltage.IV)
            .buildObject();

        GOOD_GRINDER = simple("component/grinder/good");
        ADVANCED_GRINDER = simple("component/grinder/advanced");

        BASIC_BUZZSAW = REGISTRATE.item("component/buzzsaw/basic", Item::new)
            .tint(COBALT_BRASS.color)
            .register();

        GOOD_BUZZSAW = REGISTRATE.item("component/buzzsaw/good")
            .tint(VANADIUM_STEEL.color)
            .register();

        // TODO
        ADVANCED_BUZZSAW = simple("component/buzzsaw/advanced");

        GRINDER = set3(() -> Items.DIAMOND, GOOD_GRINDER, ADVANCED_GRINDER);
        BUZZSAW = set3(BASIC_BUZZSAW, GOOD_BUZZSAW, ADVANCED_BUZZSAW);

        BOULES = new ArrayList<>();
        RAW_WAFERS = new ArrayList<>();
        WAFERS = new HashMap<>();
        CHIPS = new HashMap<>();
        boules("silicon", "glowstone", "naquadah", "neutronium");
        wafers("integrated_circuit", "cpu", "nano_cpu", "qbit_cpu",
            "ram", "nand", "nor",
            "simple_soc", "soc", "advanced_soc",
            "low_pic", "pic", "high_pic");

        FLUID_CELL = ComponentBuilder.<CellItem, MaterialSet>builder((v, mat) -> REGISTRATE
                .item("tool/fluid_cell/" + mat.name, CellItem.factory(1 << (v.rank - 1)))
                .tint(() -> () -> CellItem::getTint)
                .register())
            .voltage(Voltage.ULV, IRON)
            .voltage(Voltage.LV, STEEL)
            .voltage(Voltage.MV, ALUMINIUM)
            .buildObject();

        ITEM_FILTER = simple("component/item_filter");
        FERTILIZER = simple("misc/fertilizer");

        ME_STORAGE_CELL = REGISTRATE.item("logistics/me_storage_cell",
                properties -> new MEStorageCell(properties, 8192))
            .register();
    }

    public static void init() {}

    private static ComponentBuilder.Simple<Item, ?> componentBuilder(String name) {
        return ComponentBuilder.simple(v -> simple("component/" + v.id + "/" + name));
    }

    private static Map<Voltage, IEntry<Item>> component(String name) {
        var ret = componentBuilder(name)
            .voltages(Voltage.LV, Voltage.IV)
            .buildObject();
        COMPONENT_ITEMS.addAll(ret.values());
        return ret;
    }

    private static IEntry<Item> simple(String name) {
        return REGISTRATE.item(name, Item::new).register();
    }

    private static void boules(String... names) {
        for (var name : names) {
            BOULES.add(simple("boule/" + name));
            RAW_WAFERS.add(simple("wafer_raw/" + name));
        }
    }

    private static void wafers(String... names) {
        for (var name : names) {
            WAFERS.put(name, simple("wafer/" + name));
            CHIPS.put(name, simple("chip/" + name));
        }
    }

    private static Map<Voltage, Supplier<? extends ItemLike>> set3(
        Supplier<? extends ItemLike> basic,
        Supplier<? extends ItemLike> good,
        Supplier<? extends ItemLike> advanced) {
        return Map.of(Voltage.LV, basic, Voltage.MV, basic,
            Voltage.HV, good, Voltage.EV, good, Voltage.IV, advanced);
    }

    public static <A> boolean never(BlockState state, BlockGetter world, BlockPos pos, A val) {
        return false;
    }

    public static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }
}
