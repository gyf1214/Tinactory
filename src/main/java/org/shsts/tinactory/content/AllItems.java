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
import org.shsts.tinactory.content.electric.Circuit;
import org.shsts.tinactory.content.electric.CircuitComponent;
import org.shsts.tinactory.content.electric.CircuitLevel;
import org.shsts.tinactory.content.electric.CircuitTier;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.logistics.MEStorageCell;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.content.material.RubberTreeGrower;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.SubnetBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.CellItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.getMaterial;
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
    public static final Map<Voltage, IEntry<Item>> BUZZSAW;

    // circuits
    public static final Circuit VACUUM_TUBE;
    public static final Circuit ELECTRONIC_CIRCUIT;
    public static final Circuit GOOD_ELECTRONIC;
    public static final Circuit BASIC_INTEGRATED;
    public static final Circuit GOOD_INTEGRATED;
    public static final Circuit ADVANCED_INTEGRATED;
    public static final Circuit NAND_CHIP;
    public static final Circuit MICROPROCESSOR;
    public static final Circuit INTEGRATED_PROCESSOR;
    public static final Circuit PROCESSOR_ASSEMBLY;
    public static final Circuit WORKSTATION;
    public static final Circuit MAINFRAME;

    // circuit components
    public static final CircuitComponent RESISTOR;
    public static final CircuitComponent CAPACITOR;
    public static final CircuitComponent INDUCTOR;
    public static final CircuitComponent DIODE;
    public static final CircuitComponent TRANSISTOR;

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
    public static final List<IEntry<MEStorageCell>> ITEM_STORAGE_CELL;
    public static final List<IEntry<MEStorageCell>> FLUID_STORAGE_CELL;

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

        Circuits.buildBoards();

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
                .item("component/" + v.id + "/research_equipment")
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

        CABLE = ComponentBuilder.<CableBlock, String>builder((v, name) -> {
                var mat = getMaterial(name);
                return REGISTRATE
                    .block("network/" + v.id + "/cable", CableBlock.cable(v, mat))
                    .transform(CableBlock.tint(v, mat.color))
                    .translucent()
                    .register();
            })
            .voltage(Voltage.ULV, "iron")
            .voltage(Voltage.LV, "tin")
            .voltage(Voltage.MV, "copper")
            .voltage(Voltage.HV, "gold")
            .voltage(Voltage.EV, "aluminium")
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

        BASIC_BUZZSAW = REGISTRATE.item("component/buzzsaw/basic")
            .tint(getMaterial("cobalt_brass").color)
            .register();

        GOOD_BUZZSAW = REGISTRATE.item("component/buzzsaw/good")
            .tint(getMaterial("vanadium_steel").color)
            .register();

        // TODO: tint
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

        FLUID_CELL = ComponentBuilder.<CellItem, String>builder((v, name) -> REGISTRATE
                .item("tool/fluid_cell/" + name, CellItem.factory(1 << (v.rank - 1)))
                .tint(() -> () -> CellItem::getTint)
                .register())
            .voltage(Voltage.ULV, "iron")
            .voltage(Voltage.LV, "steel")
            .voltage(Voltage.MV, "aluminium")
            .buildObject();

        ITEM_FILTER = simple("component/item_filter");
        FERTILIZER = simple("misc/fertilizer");

        ITEM_STORAGE_CELL = new ArrayList<>();
        FLUID_STORAGE_CELL = new ArrayList<>();
        for (var i = 0; i < 5; i++) {
            var k = 1 << (2 * i);
            var bytes = 1048576 * k;
            ITEM_STORAGE_CELL.add(REGISTRATE.item(
                "logistics/item_storage_cell/" + k + "m",
                MEStorageCell.itemCell(bytes)).register());
            FLUID_STORAGE_CELL.add(REGISTRATE.item(
                "logistics/fluid_storage_cell/" + k + "m",
                MEStorageCell.fluidCell(bytes)).register());
        }
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
        return REGISTRATE.item(name).register();
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

    private static <S extends Supplier<? extends ItemLike>> Map<Voltage, S> set3(
        S basic, S good, S advanced) {
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
