package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.BoilerPlugin;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.PrimitiveMachine;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.content.network.SidedMachineBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.api.logistics.SlotType.FLUID_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.FLUID_OUTPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_OUTPUT;
import static org.shsts.tinactory.content.machine.ProcessingSet.generator;
import static org.shsts.tinactory.content.machine.ProcessingSet.machine;
import static org.shsts.tinactory.content.machine.ProcessingSet.marker;
import static org.shsts.tinactory.content.machine.ProcessingSet.research;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final ProcessingSet RESEARCH_BENCH;
    public static final ProcessingSet ASSEMBLER;
    public static final ProcessingSet STONE_GENERATOR;
    public static final ProcessingSet ORE_ANALYZER;
    public static final ProcessingSet MACERATOR;
    public static final ProcessingSet ORE_WASHER;
    public static final ProcessingSet CENTRIFUGE;
    public static final ProcessingSet THERMAL_CENTRIFUGE;
    public static final MachineSet ELECTRIC_FURNACE;
    public static final ProcessingSet ALLOY_SMELTER;
    public static final ProcessingSet POLARIZER;
    public static final ProcessingSet EXTRACTOR;
    public static final ProcessingSet FLUID_SOLIDIFIER;
    public static final ProcessingSet STEAM_TURBINE;
    public static final MachineSet BATTERY_BOX;
    public static final MachineSet ELECTRIC_CHEST;

    public static final RegistryEntry<MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> WORKBENCH;
    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_STONE_GENERATOR;
    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_ORE_ANALYZER;
    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_ORE_WASHER;
    public static final RegistryEntry<MachineBlock<SmartBlockEntity>> LOW_PRESSURE_BOILER;
    public static final RegistryEntry<MachineBlock<SmartBlockEntity>> HIGH_PRESSURE_BOILER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> BLAST_FURNACE;
    public static final RegistryEntry<SidedMachineBlock<SmartBlockEntity>> MULTI_BLOCK_INTERFACE;

    public static final Set<ProcessingSet> PROCESSING_SETS;

    static {
        PROCESSING_SETS = new HashSet<>();

        RESEARCH_BENCH = set(research())
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .placeHolder(new Rect(3 * SLOT_SIZE, SLOT_SIZE / 2 - 2, 24, 24))
                .progressBar(Texture.PROGRESS_MULTIPLE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        var assembler = set(machine(AllRecipes.ASSEMBLER))
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT);
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < (i == 2 ? 2 : 3); j++) {
                assembler.slot(SLOT_SIZE * j, 1 + SLOT_SIZE * i);
            }
        }
        ASSEMBLER = assembler.port(FLUID_INPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE * 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_CIRCUIT, 8 + SLOT_SIZE * 3, SLOT_SIZE)
                .build()
                .buildObject();

        STONE_GENERATOR = set(machine(AllRecipes.STONE_GENERATOR))
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE / 2)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .progressBar(Texture.PROGRESS_MACERATE, 8, SLOT_SIZE / 2)
                .build()
                .buildObject();

        var oreAnalyzer = set(ProcessingSet.oreAnalyzer())
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_SIFT, 8 + SLOT_SIZE, SLOT_SIZE / 2);
        for (var i = 3; i < 6; i++) {
            var startVoltage = i == 5 ? Voltage.LV : Voltage.PRIMITIVE;
            oreAnalyzer.port(ITEM_OUTPUT)
                    .slot(SLOT_SIZE * i, 1 + SLOT_SIZE / 2, startVoltage, Voltage.MV)
                    .slot(SLOT_SIZE * i, 1, Voltage.HV)
                    .slot(SLOT_SIZE * i, 1 + SLOT_SIZE, Voltage.HV);
        }
        ORE_ANALYZER = oreAnalyzer.build().buildObject();

        MACERATOR = set(marker(AllRecipes.MACERATOR, true))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.LV, Voltage.HV)
                .slot(SLOT_SIZE * 3, 1, Voltage.EV)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2, Voltage.MV, Voltage.HV)
                .slot(SLOT_SIZE * 4, 1, Voltage.EV)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2, List.of(Voltage.HV))
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE, Voltage.EV)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE, Voltage.EV)
                .progressBar(Texture.PROGRESS_MACERATE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ORE_WASHER = set(marker(AllRecipes.ORE_WASHER, false))
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(FLUID_INPUT)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .progressBar(Texture.PROGRESS_BATH, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        CENTRIFUGE = set(marker(AllRecipes.CENTRIFUGE, true))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(FLUID_INPUT)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1).slot(SLOT_SIZE * 5, 1).slot(SLOT_SIZE * 6, 1)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE).slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_EXTRACT, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        THERMAL_CENTRIFUGE = set(machine(AllRecipes.THERMAL_CENTRIFUGE))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ELECTRIC_FURNACE = ProcessingSet.electricFurnace()
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ALLOY_SMELTER = set(machine(AllRecipes.ALLOY_SMELTER))
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        POLARIZER = set(machine(AllRecipes.POLARIZER))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_MAGNETIC, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        EXTRACTOR = set(machine(AllRecipes.EXTRACTOR))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_EXTRACT, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        FLUID_SOLIDIFIER = set(machine(AllRecipes.FLUID_SOLIDIFIER))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        STEAM_TURBINE = set(generator(AllRecipes.STEAM_TURBINE))
                .voltages(Voltage.ULV, Voltage.HV)
                .layoutSet()
                .port(FLUID_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_GAS, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        LOW_PRESSURE_BOILER = boiler("low", 5d);
        HIGH_PRESSURE_BOILER = boiler("high", 17d);

        BLAST_FURNACE = REGISTRATE.blockEntity("multi_block/blast_furnace",
                        PrimitiveBlock<SmartBlockEntity>::new)
                .blockEntity()
                .eventManager().ticking()
                .simpleCapability(MultiBlock::blastFurnace)
                .simpleCapability(RecipeProcessor.multiBlock(AllRecipes.BLAST_FURNACE))
                .build()
                .translucent()
                .buildObject();

        MULTI_BLOCK_INTERFACE = REGISTRATE.blockEntity("multi_block/interface",
                        MachineBlock.sided(Voltage.LV))
                .blockEntity()
                .eventManager()
                .simpleCapability(MultiBlockInterface::basic)
                .simpleCapability(FlexibleStackContainer::builder)
                .menu(ProcessingMenu.multiBlock())
                .title(ProcessingMenu::getTitle)
                .plugin(MachinePlugin::multiBlock)
                .build()
                .build()
                .translucent()
                .buildObject();

        var batteryBox = ProcessingSet.batteryBox()
                .voltages(Voltage.LV, Voltage.HV)
                .layoutSet()
                .port(ITEM_INPUT);
        for (var i = 0; i < 4; i++) {
            for (var j = 0; j < 4; j++) {
                batteryBox.slot(j * SLOT_SIZE, i * SLOT_SIZE, Voltage.fromRank(1 + Math.max(i, j)));
            }
        }
        BATTERY_BOX = batteryBox.build().buildObject();

        var electricChest = ProcessingSet.electricChest()
                .voltages(Voltage.ULV, Voltage.HV)
                .layoutSet()
                .port(SlotType.NONE);
        for (var i = 0; i < 2; i++) {
            for (var j = 0; j < 8; j++) {
                var voltage = Voltage.fromValue(8 * (j + 1) * (j + 1));
                electricChest.slot(j * (SLOT_SIZE + 2), 1 + i * 2 * (SLOT_SIZE + MARGIN_VERTICAL), voltage);
            }
        }
        ELECTRIC_CHEST = electricChest.build().buildObject();

        NETWORK_CONTROLLER = REGISTRATE.blockEntity("network/controller",
                        NetworkController::new,
                        MachineBlock.factory(Voltage.PRIMITIVE))
                .entityClass(NetworkController.class)
                .blockEntity()
                .eventManager().ticking()
                .menu(NetworkControllerMenu::new)
                .noInventory()
                .title("networkController")
                .build()
                .build()
                .translucent()
                .buildObject();

        WORKBENCH = REGISTRATE.blockEntity("primitive/workbench",
                        PrimitiveBlock<SmartBlockEntity>::new)
                .blockEntity()
                .simpleCapability(Workbench::builder)
                .menu(WorkbenchMenu::new).build()
                .build()
                .buildObject();

        PRIMITIVE_STONE_GENERATOR = primitive(STONE_GENERATOR);
        PRIMITIVE_ORE_ANALYZER = primitive(ORE_ANALYZER);
        PRIMITIVE_ORE_WASHER = primitive(ORE_WASHER);
    }

    public static void init() {}

    private static RegistryEntry<PrimitiveBlock<PrimitiveMachine>>
    primitive(ProcessingSet set) {
        var recipeType = set.recipeType;
        var id = "primitive/" + recipeType.id;
        var layout = set.layout(Voltage.PRIMITIVE);
        return REGISTRATE.blockEntity(id, PrimitiveMachine::new,
                        PrimitiveBlock<PrimitiveMachine>::new)
                .entityClass(PrimitiveMachine.class)
                .blockEntity()
                .eventManager().ticking()
                .simpleCapability(RecipeProcessor.machine(recipeType))
                .simpleCapability(StackProcessingContainer.builder(layout))
                .menu(ProcessingMenu.machine(layout))
                .title(ProcessingMenu::getTitle)
                .build()
                .build()
                .translucent()
                .buildObject();
    }

    private static <T extends ProcessingRecipe> ProcessingSet.Builder<T, ?>
    set(ProcessingSet.Builder<T, ?> builder) {
        return builder.onCreateObject(PROCESSING_SETS::add);
    }

    private static RegistryEntry<MachineBlock<SmartBlockEntity>>
    boiler(String name, double burnSpeed) {
        var id = "machine/boiler/" + name;
        var layout = AllLayouts.BOILER;
        return REGISTRATE.blockEntity(id, MachineBlock.factory(Voltage.PRIMITIVE))
                .blockEntity()
                .eventManager()
                .simpleCapability(Machine::builder)
                .simpleCapability(Boiler.builder(burnSpeed))
                .simpleCapability(StackProcessingContainer.builder(layout))
                .menu(ProcessingMenu.machine(layout))
                .title(ProcessingMenu::getTitle)
                .plugin(MachinePlugin::noBook)
                .plugin(BoilerPlugin::new)
                .build()
                .build()
                .translucent()
                .buildObject();
    }
}
