package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.content.machine.ElectricTank;
import org.shsts.tinactory.content.machine.MEDriver;
import org.shsts.tinactory.content.machine.MEStorageInterface;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.PrimitiveMachine;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.api.logistics.SlotType.FLUID_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.FLUID_OUTPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_OUTPUT;
import static org.shsts.tinactory.content.machine.MachineSet.baseMachine;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_ARROW;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_BATH;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_BENDING;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_CIRCUIT;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_CIRCUIT_ASSEMBLER;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_EXTRACT;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_EXTRUDER;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_GAS;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_LATHE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_LATH_BASE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_MACERATE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_MAGNETIC;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_MIXER;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_MULTIPLE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_SIFT;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_SLICE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_WIREMILL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final ProcessingSet RESEARCH_BENCH;
    public static final ProcessingSet ASSEMBLER;
    public static final ProcessingSet LASER_ENGRAVER;
    public static final ProcessingSet CIRCUIT_ASSEMBLER;
    public static final ProcessingSet STONE_GENERATOR;
    public static final ProcessingSet ORE_ANALYZER;
    public static final ProcessingSet MACERATOR;
    public static final ProcessingSet ORE_WASHER;
    public static final ProcessingSet CENTRIFUGE;
    public static final ProcessingSet THERMAL_CENTRIFUGE;
    public static final MachineSet ELECTRIC_FURNACE;
    public static final ProcessingSet ALLOY_SMELTER;
    public static final ProcessingSet MIXER;
    public static final ProcessingSet POLARIZER;
    public static final ProcessingSet WIREMILL;
    public static final ProcessingSet BENDER;
    public static final ProcessingSet LATHE;
    public static final ProcessingSet CUTTER;
    public static final ProcessingSet EXTRUDER;
    public static final ProcessingSet EXTRACTOR;
    public static final ProcessingSet FLUID_SOLIDIFIER;
    public static final ProcessingSet ELECTROLYZER;
    public static final ProcessingSet CHEMICAL_REACTOR;
    public static final ProcessingSet ARC_FURNACE;
    public static final ProcessingSet STEAM_TURBINE;
    public static final ProcessingSet GAS_TURBINE;
    public static final ProcessingSet COMBUSTION_GENERATOR;
    public static final MachineSet BATTERY_BOX;
    public static final MachineSet ELECTRIC_CHEST;
    public static final MachineSet ELECTRIC_TANK;
    public static final MachineSet LOGISTIC_WORKER;
    public static final MachineSet ME_DRIVER;
    public static final MachineSet ME_STORAGE_INTERFACE;
    public static final Map<Voltage, IEntry<MachineBlock>> MULTIBLOCK_INTERFACE;

    public static final IEntry<MachineBlock> NETWORK_CONTROLLER;
    public static final IEntry<PrimitiveBlock> WORKBENCH;
    public static final IEntry<PrimitiveBlock> PRIMITIVE_STONE_GENERATOR;
    public static final IEntry<PrimitiveBlock> PRIMITIVE_ORE_ANALYZER;
    public static final IEntry<PrimitiveBlock> PRIMITIVE_ORE_WASHER;
    public static final IEntry<MachineBlock> LOW_PRESSURE_BOILER;
    public static final IEntry<MachineBlock> HIGH_PRESSURE_BOILER;

    public static final Set<ProcessingSet> PROCESSING_SETS;

    static {
        PROCESSING_SETS = new HashSet<>();

        var set = new SetFactory();

        RESEARCH_BENCH = set.processing(AllRecipes.RESEARCH_BENCH)
            .menu(AllMenus.RESEARCH_BENCH)
            .voltages(Voltage.ULV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .placeHolder(new Rect(3 * SLOT_SIZE, SLOT_SIZE / 2 - 2, 24, 24))
            .progressBar(PROGRESS_MULTIPLE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        ASSEMBLER = set.processing(AllRecipes.ASSEMBLER)
            .processor(RecipeProcessor::noAutoRecipe)
            .voltages(Voltage.ULV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 1, 2, 3)
            .slots(0, 1 + SLOT_SIZE * 2, 1, 2)
            .port(FLUID_INPUT)
            .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE * 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
            .progressBar(PROGRESS_CIRCUIT, 8 + SLOT_SIZE * 3, SLOT_SIZE)
            .build()
            .buildObject();

        LASER_ENGRAVER = set.processing(AllRecipes.LASER_ENGRAVER)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_INPUT)
            .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_ARROW, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
            .build()
            .buildObject();

        CIRCUIT_ASSEMBLER = set.processing(AllRecipes.CIRCUIT_ASSEMBLER)
            .processor(RecipeProcessor::noAutoRecipe)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 1, 2, 3)
            .port(FLUID_INPUT)
            .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE * 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_CIRCUIT_ASSEMBLER, 8 + SLOT_SIZE * 3, SLOT_SIZE / 2)
            .build()
            .buildObject();

        STONE_GENERATOR = set.processing(AllRecipes.STONE_GENERATOR)
            .processor(RecipeProcessor::noAutoRecipe)
            .voltages(Voltage.ULV)
            .layoutSet()
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE / 2)
            .port(FLUID_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.ULV)
            .progressBar(PROGRESS_MACERATE, 8, SLOT_SIZE / 2)
            .build()
            .buildObject();

        ORE_ANALYZER = set.processing(AllRecipes.ORE_ANALYZER)
            .processor($ -> RecipeProcessor::oreProcessor)
            .menu(AllMenus.MARKER)
            .voltages(Voltage.ULV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slots(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, 1, 2, Voltage.PRIMITIVE, Voltage.ULV)
            .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2, List.of(Voltage.ULV))
            .slots(SLOT_SIZE * 3, 1, 2, 3, Voltage.LV)
            .progressBar(PROGRESS_SIFT, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        MACERATOR = set.processing(AllRecipes.MACERATOR)
            .menu(AllMenus.MARKER_WITH_NORMAL)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.LV, Voltage.HV)
            .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2, Voltage.MV, Voltage.HV)
            .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2, List.of(Voltage.HV))
            .slots(SLOT_SIZE * 3, 1, 2, 2, Voltage.EV)
            .progressBar(PROGRESS_MACERATE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        ORE_WASHER = set.processing(AllRecipes.ORE_WASHER)
            .menu(AllMenus.MARKER_WITH_NORMAL)
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
            .progressBar(PROGRESS_BATH, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
            .build()
            .buildObject();

        CENTRIFUGE = set.processing(AllRecipes.CENTRIFUGE)
            .menu(AllMenus.MARKER_WITH_NORMAL)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(FLUID_INPUT)
            .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slots(SLOT_SIZE * 4, 1, 1, 3)
            .port(FLUID_OUTPUT)
            .slots(SLOT_SIZE * 4, 1 + SLOT_SIZE, 1, 3)
            .progressBar(PROGRESS_EXTRACT, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
            .build()
            .buildObject();

        THERMAL_CENTRIFUGE = set.processing(AllRecipes.THERMAL_CENTRIFUGE)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        ELECTRIC_FURNACE = set.machine()
            .machine(v -> "machine/" + v.id + "/electric_furnace", MachineBlock::factory)
            .menu(AllMenus.ELECTRIC_FURNACE)
            .layoutMachine(StackProcessingContainer::factory)
            .machine(RecipeProcessor::electricFurnace)
            .tintVoltage(2)
            .voltages(Voltage.ULV)
            .transform(simpleLayout(PROGRESS_ARROW))
            .buildObject();

        ALLOY_SMELTER = set.processing(AllRecipes.ALLOY_SMELTER)
            .voltages(Voltage.ULV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 1 + SLOT_SIZE / 2, 1, 3)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2)
            .port(FLUID_OUTPUT)
            .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_ARROW, 8 + SLOT_SIZE * 3, SLOT_SIZE / 2)
            .build()
            .buildObject();

        MIXER = set.processing(AllRecipes.MIXER)
            .processor(RecipeProcessor::noAutoRecipe)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 1, 2, 3)
            .port(FLUID_INPUT)
            .slots(SLOT_SIZE, 1 + SLOT_SIZE * 2, 1, 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
            .port(FLUID_OUTPUT)
            .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE)
            .progressBar(PROGRESS_MIXER, 8 + SLOT_SIZE * 3, SLOT_SIZE)
            .build()
            .buildObject();

        POLARIZER = set.simpleMachine(AllRecipes.POLARIZER, PROGRESS_MAGNETIC);
        WIREMILL = set.simpleMachine(AllRecipes.WIREMILL, PROGRESS_WIREMILL);
        BENDER = set.simpleMachine(AllRecipes.BENDER, PROGRESS_BENDING);

        LATHE = set.processing(AllRecipes.LATHE)
            .transform(simpleLayout(PROGRESS_LATHE))
            .layoutSet()
            .image(28 + SLOT_SIZE, 1 + SLOT_SIZE / 2, PROGRESS_LATH_BASE)
            .build()
            .buildObject();

        CUTTER = set.processing(AllRecipes.CUTTER)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(FLUID_INPUT)
            .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_SLICE, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
            .build()
            .buildObject();

        EXTRUDER = set.processing(AllRecipes.EXTRUDER)
            .voltages(Voltage.MV)
            .processor(RecipeProcessor::noAutoRecipe)
            .transform(simpleLayout(PROGRESS_EXTRUDER))
            .buildObject();

        EXTRACTOR = set.processing(AllRecipes.EXTRACTOR)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
            .port(FLUID_OUTPUT)
            .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_EXTRACT, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        FLUID_SOLIDIFIER = set.processing(AllRecipes.FLUID_SOLIDIFIER)
            .processor(RecipeProcessor::noAutoRecipe)
            .layoutSet()
            .port(FLUID_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        ELECTROLYZER = set.processing(AllRecipes.ELECTROLYZER)
            .voltages(Voltage.MV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .port(FLUID_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slots(SLOT_SIZE * 4, 1, 1, 3)
            .port(FLUID_OUTPUT)
            .slots(SLOT_SIZE * 4, 1 + SLOT_SIZE, 1, 3)
            .progressBar(PROGRESS_EXTRACT, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
            .build()
            .buildObject();

        CHEMICAL_REACTOR = set.processing(AllRecipes.CHEMICAL_REACTOR)
            .processor(RecipeProcessor::noAutoRecipe)
            .voltages(Voltage.MV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 1, 1, 2)
            .port(FLUID_INPUT)
            .slots(0, 1 + SLOT_SIZE, 1, 2)
            .port(ITEM_OUTPUT)
            .slots(SLOT_SIZE * 4, 1, 1, 2)
            .port(FLUID_OUTPUT)
            .slots(SLOT_SIZE * 4, 1 + SLOT_SIZE, 1, 2)
            .progressBar(PROGRESS_MIXER, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
            .build()
            .buildObject();

        ARC_FURNACE = set.processing(AllRecipes.ARC_FURNACE)
            .voltages(Voltage.HV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1)
            .port(FLUID_INPUT)
            .slot(SLOT_SIZE, 1)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 4, 1)
            .progressBar(PROGRESS_SLICE, 8 + SLOT_SIZE * 2, 0)
            .build()
            .buildObject();

        STEAM_TURBINE = set.processing(AllRecipes.STEAM_TURBINE)
            .processor(RecipeProcessor::generator)
            .voltages(Voltage.ULV, Voltage.HV)
            .layoutSet()
            .port(FLUID_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(FLUID_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_GAS, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        GAS_TURBINE = set.processing(AllRecipes.GAS_TURBINE)
            .processor(RecipeProcessor::generator)
            .voltages(Voltage.LV, Voltage.HV)
            .layoutSet()
            .port(FLUID_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_GAS, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        COMBUSTION_GENERATOR = set.processing(AllRecipes.COMBUSTION_GENERATOR)
            .processor(RecipeProcessor::generator)
            .voltages(Voltage.LV, Voltage.HV)
            .layoutSet()
            .port(FLUID_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .progressBar(PROGRESS_GAS, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build()
            .buildObject();

        LOW_PRESSURE_BOILER = boiler("low", 5d);
        HIGH_PRESSURE_BOILER = boiler("high", 17d);

        MULTIBLOCK_INTERFACE = ComponentBuilder
            .simple(ProcessingSet::multiblockInterface)
            .voltages(Voltage.ULV, Voltage.LuV)
            .buildObject();

        BATTERY_BOX = set.machine()
            .machine(v -> "machine/" + v.id + "/battery_box", MachineBlock::sided)
            .menu(AllMenus.SIMPLE_MACHINE)
            .layoutMachine(BatteryBox::factory)
            .voltages(Voltage.LV, Voltage.HV)
            .layoutSet()
            .port(ITEM_INPUT)
            .transform($ -> {
                for (var i = 0; i < 4; i++) {
                    for (var j = 0; j < 4; j++) {
                        $.slot(j * SLOT_SIZE, i * SLOT_SIZE, Voltage.fromRank(1 + Math.max(i, j)));
                    }
                }
                return $;
            }).build()
            .tintVoltage(0)
            .buildObject();

        ELECTRIC_CHEST = set.machine()
            .machine(v -> "machine/" + v.id + "/electric_chest", MachineBlock::factory)
            .menu(AllMenus.ELECTRIC_CHEST)
            .layoutMachine(ElectricChest::factory)
            .voltages(Voltage.ULV, Voltage.HV)
            .layoutSet()
            .port(SlotType.NONE)
            .transform($ -> {
                for (var i = 0; i < 2; i++) {
                    for (var j = 0; j < 8; j++) {
                        var voltage = Voltage.fromValue(8 * (j + 1) * (j + 1));
                        $.slot(j * (SLOT_SIZE + 2), 1 + i * 2 * (SLOT_SIZE + MARGIN_VERTICAL), voltage);
                    }
                }
                return $;
            }).build()
            .tintVoltage(2)
            .buildObject();

        ELECTRIC_TANK = set.machine()
            .machine(v -> "machine/" + v.id + "/electric_tank", MachineBlock::factory)
            .menu(AllMenus.ELECTRIC_TANK)
            .layoutMachine(ElectricTank::factory)
            .voltages(Voltage.ULV, Voltage.HV)
            .layoutSet()
            .port(FLUID_INPUT)
            .transform($ -> {
                for (var i = 0; i < 8; i++) {
                    var voltage = Voltage.fromValue(8 * (i + 1) * (i + 1));
                    $.slot(i * (SLOT_SIZE + 2), 1, voltage);
                }
                return $;
            }).build()
            .tintVoltage(2)
            .buildObject();

        LOGISTIC_WORKER = set.machine()
            .machine(v -> "logistics/" + v.id + "/logistic_worker", MachineBlock::factory)
            .menu(AllMenus.LOGISTIC_WORKER)
            .machine(LogisticWorker::factory)
            .voltages(Voltage.ULV)
            .tintVoltage(2)
            .buildObject();

        ME_DRIVER = set.machine()
            .machine(v -> "logistics/" + v.id + "/me_driver", MachineBlock::factory)
            .menu(AllMenus.SIMPLE_MACHINE)
            .layoutMachine(MEDriver::factory)
            .voltages(Voltage.HV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 0, 3, 3)
            .build()
            .tintVoltage(2)
            .buildObject();

        ME_STORAGE_INTERFACE = set.machine()
            .machine(v -> "logistics/" + v.id + "/me_storage_interface", MachineBlock::factory)
            .voltages(Voltage.HV)
            .machine(MEStorageInterface::factory)
            .tintVoltage(2)
            .buildObject();

        NETWORK_CONTROLLER = set.blockEntity("network/controller",
                MachineBlock.factory(Voltage.PRIMITIVE))
            .menu(AllMenus.NETWORK_CONTROLLER)
            .blockEntity()
            .transform(NetworkController::factory)
            .end()
            .translucent()
            .buildObject();

        WORKBENCH = set.blockEntity("primitive/workbench",
                PrimitiveBlock::new)
            .menu(AllMenus.WORKBENCH)
            .blockEntity()
            .transform(Workbench::factory)
            .end()
            .buildObject();

        PRIMITIVE_STONE_GENERATOR = primitive(STONE_GENERATOR);
        PRIMITIVE_ORE_ANALYZER = primitive(ORE_ANALYZER);
        PRIMITIVE_ORE_WASHER = primitive(ORE_WASHER);
    }

    public static void init() {}

    public static Set<ProcessingSet> getProcessingSets() {
        return PROCESSING_SETS;
    }

    private static class SetFactory {
        public <U extends SmartEntityBlock> BlockEntityBuilder<U, SetFactory> blockEntity(
            String id, SmartEntityBlock.Factory<U> factory) {
            return BlockEntityBuilder.builder(this, id, factory);
        }

        public MachineSet.Builder<SetFactory> machine() {
            return MachineSet.builder(this);
        }

        public <R extends ProcessingRecipe, B extends IRecipeBuilder<R, B>> ProcessingSet.Builder<R,
            B, SetFactory> processing(IRecipeType<B> recipeType) {
            return ProcessingSet.builder(this, recipeType)
                .tintVoltage(2)
                .onCreateObject(PROCESSING_SETS::add);
        }

        public <R extends ProcessingRecipe, B extends IRecipeBuilder<R, B>> ProcessingSet simpleMachine(
            IRecipeType<B> recipeType, Texture progressBar) {
            return processing(recipeType)
                .transform(simpleLayout(progressBar))
                .buildObject();
        }
    }

    private static IEntry<PrimitiveBlock> primitive(ProcessingSet set) {
        var id = "primitive/" + set.recipeType.id();
        var layout = set.layout(Voltage.PRIMITIVE);
        return BlockEntityBuilder.builder(set, id, PrimitiveBlock::new)
            .menu(AllMenus.PRIMITIVE_MACHINE)
            .blockEntity()
            .transform(PrimitiveMachine::factory)
            .transform(set.mapRecipeType(RecipeProcessor::machine))
            .transform(StackProcessingContainer.factory(layout))
            .end()
            .translucent()
            .buildObject();
    }

    private static IEntry<MachineBlock> boiler(String name, double burnSpeed) {
        var id = "machine/boiler/" + name;
        var layout = AllLayouts.BOILER;
        return BlockEntityBuilder.builder(id,
                MachineBlock.factory(Voltage.PRIMITIVE))
            .menu(AllMenus.BOILER)
            .blockEntity()
            .transform(Boiler.factory(burnSpeed))
            .transform(StackProcessingContainer.factory(layout))
            .end()
            .transform(baseMachine())
            .buildObject();
    }

    private static <S extends MachineSet.BuilderBase<?, ?,
        S>> Transformer<S> simpleLayout(Texture progressBar) {
        return $ -> $.layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
            .progressBar(progressBar, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build();
    }
}
