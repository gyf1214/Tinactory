package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.BoilerPlugin;
import org.shsts.tinactory.content.gui.ElectricChestMenu;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.ResearchBenchPlugin;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.PrimitiveMachine;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.api.logistics.SlotType.FLUID_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.FLUID_OUTPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_OUTPUT;
import static org.shsts.tinactory.content.machine.MachineSet.baseMachine;
import static org.shsts.tinactory.content.machine.ProcessingSet.marker;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final ProcessingSet RESEARCH_BENCH;
    public static final ProcessingSet ASSEMBLER;
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
    public static final ProcessingSet COMPRESSOR;
    public static final ProcessingSet LATHE;
    public static final ProcessingSet CUTTER;
    public static final ProcessingSet EXTRACTOR;
    public static final ProcessingSet FLUID_SOLIDIFIER;
    public static final ProcessingSet STEAM_TURBINE;
    public static final MachineSet BATTERY_BOX;
    public static final MachineSet ELECTRIC_CHEST;
    public static final Map<Voltage, RegistryEntry<MachineBlock<SmartBlockEntity>>> MULTI_BLOCK_INTERFACE;

    public static final RegistryEntry<MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> WORKBENCH;
    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_STONE_GENERATOR;
    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_ORE_ANALYZER;
    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_ORE_WASHER;
    public static final RegistryEntry<MachineBlock<SmartBlockEntity>> LOW_PRESSURE_BOILER;
    public static final RegistryEntry<MachineBlock<SmartBlockEntity>> HIGH_PRESSURE_BOILER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> BLAST_FURNACE;

    public static final Set<ProcessingSet> PROCESSING_SETS;

    static {
        PROCESSING_SETS = new HashSet<>();

        var set = new SetFactory();

        RESEARCH_BENCH = set.processing(AllRecipes.RESEARCH_BENCH)
                .processingPlugin(MachinePlugin::processing)
                .processingPlugin(r -> ResearchBenchPlugin.builder())
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .placeHolder(new Rect(3 * SLOT_SIZE, SLOT_SIZE / 2 - 2, 24, 24))
                .progressBar(Texture.PROGRESS_MULTIPLE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ASSEMBLER = set.processing(AllRecipes.ASSEMBLER)
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slots(0, 1, 2, 3)
                .slots(0, 1 + SLOT_SIZE * 2, 1, 2)
                .port(FLUID_INPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE * 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_CIRCUIT, 8 + SLOT_SIZE * 3, SLOT_SIZE)
                .build()
                .buildObject();

        CIRCUIT_ASSEMBLER = set.processing(AllRecipes.CIRCUIT_ASSEMBLER)
                .layoutSet()
                .port(ITEM_INPUT)
                .slots(0, 1, 2, 3)
                .port(FLUID_INPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE * 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_CIRCUIT, 8 + SLOT_SIZE * 3, SLOT_SIZE / 2)
                .build()
                .buildObject();

        STONE_GENERATOR = set.processing(AllRecipes.STONE_GENERATOR)
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE / 2)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .progressBar(Texture.PROGRESS_MACERATE, 8, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ORE_ANALYZER = set.processing(AllRecipes.ORE_ANALYZER)
                .processor(r -> RecipeProcessor::oreProcessor)
                .transform(marker(false))
                .voltages(Voltage.ULV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_SIFT, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .transform($ -> {
                    for (var i = 3; i < 6; i++) {
                        var startVoltage = i == 5 ? Voltage.LV : Voltage.PRIMITIVE;
                        $.port(ITEM_OUTPUT)
                                .slot(SLOT_SIZE * i, 1 + SLOT_SIZE / 2, startVoltage, Voltage.MV)
                                .slot(SLOT_SIZE * i, 1, Voltage.HV)
                                .slot(SLOT_SIZE * i, 1 + SLOT_SIZE, Voltage.HV);
                    }
                    return $;
                }).build().buildObject();

        MACERATOR = set.processing(AllRecipes.MACERATOR)
                .transform(marker(true))
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

        ORE_WASHER = set.processing(AllRecipes.ORE_WASHER)
                .transform(marker(true))
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

        CENTRIFUGE = set.processing(AllRecipes.CENTRIFUGE)
                .transform(marker(true))
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(FLUID_INPUT)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slots(SLOT_SIZE * 4, 1, 1, 3)
                .port(FLUID_OUTPUT)
                .slots(SLOT_SIZE * 4, 1 + SLOT_SIZE, 1, 3)
                .progressBar(Texture.PROGRESS_EXTRACT, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        THERMAL_CENTRIFUGE = set.processing(AllRecipes.THERMAL_CENTRIFUGE)
                .layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slots(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, 1, 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ELECTRIC_FURNACE = set.machine()
                .machine(v -> "machine/" + v.id + "/electric_furnace", MachineBlock::sided)
                .layoutCapability(StackProcessingContainer::builder)
                .capability(RecipeProcessor::electricFurnace)
                .layoutMenu(ProcessingMenu::machine)
                .layoutPlugin(MachinePlugin::electricFurnace)
                .voltages(Voltage.ULV)
                .transform(simpleLayout(Texture.PROGRESS_ARROW))
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
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE * 3, SLOT_SIZE / 2)
                .build()
                .buildObject();

        MIXER = set.processing(AllRecipes.MIXER)
                .voltages(Voltage.LV)
                .layoutSet()
                .port(ITEM_INPUT)
                .slots(0, 1, 2, 3)
                .port(FLUID_INPUT)
                .slots(SLOT_SIZE, 1 + SLOT_SIZE * 2, 1, 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .port(FLUID_OUTPUT)
                .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_MIXER, 8 + SLOT_SIZE * 3, SLOT_SIZE)
                .build()
                .buildObject();

        POLARIZER = set.simpleMachine(AllRecipes.POLARIZER, Texture.PROGRESS_MAGNETIC);
        WIREMILL = set.simpleMachine(AllRecipes.WIREMILL, Texture.PROCESS_WIREMILL);
        BENDER = set.simpleMachine(AllRecipes.BENDER, Texture.PROCESS_BENDING);
        COMPRESSOR = set.simpleMachine(AllRecipes.COMPRESSOR, Texture.PROGRESS_COMPRESS);

        LATHE = set.processing(AllRecipes.LATHE)
                .transform(simpleLayout(Texture.PROCESS_LATHE))
                .layoutSet()
                .image(28 + SLOT_SIZE, 1 + SLOT_SIZE / 2, Texture.PROGRESS_LATH_BASE)
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
                .progressBar(Texture.PROCESS_SLICE, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        EXTRACTOR = set.processing(AllRecipes.EXTRACTOR)
                .voltages(Voltage.LV)
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

        FLUID_SOLIDIFIER = set.processing(AllRecipes.FLUID_SOLIDIFIER)
                .voltages(Voltage.LV)
                .layoutSet()
                .port(FLUID_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
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

        MULTI_BLOCK_INTERFACE = ComponentBuilder.dummy(ProcessingSet::multiblockInterface)
                .voltages(Voltage.ULV, Voltage.LuV)
                .buildObject();

        BATTERY_BOX = set.machine()
                .machine(v -> "machine/" + v.id + "/battery_box", MachineBlock::sided)
                .capability(BatteryBox::builder)
                .layoutMenu(ProcessingMenu::machine)
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
                .buildObject();

        ELECTRIC_CHEST = set.machine()
                .machine(v -> "machine/" + v.id + "/electric_chest", MachineBlock::factory)
                .layoutCapability(ElectricChest::builder)
                .layoutMenu(ElectricChestMenu::factory)
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
                }).build().buildObject();

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

    public static Set<ProcessingSet> getProcessingSets() {
        return PROCESSING_SETS;
    }

    private static class SetFactory {
        public <T extends ProcessingRecipe> ProcessingSet.Builder<T, SetFactory>
        processing(RecipeTypeEntry<T, ?> recipeType) {
            return (new ProcessingSet.Builder<>(REGISTRATE, recipeType, this))
                    .onCreateObject(PROCESSING_SETS::add);
        }

        public <T extends ProcessingRecipe> ProcessingSet
        simpleMachine(RecipeTypeEntry<T, ?> recipeType, Texture progress) {
            return processing(recipeType)
                    .transform(simpleLayout(progress))
                    .buildObject();
        }

        public MachineSet.Builder<SetFactory> machine() {
            return new MachineSet.Builder<>(REGISTRATE, this);
        }
    }

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
                .menu(ProcessingMenu.machine(layout, recipeType))
                .title(ProcessingMenu::getTitle)
                .build()
                .build()
                .translucent()
                .buildObject();
    }

    private static RegistryEntry<MachineBlock<SmartBlockEntity>>
    boiler(String name, double burnSpeed) {
        var id = "machine/boiler/" + name;
        var layout = AllLayouts.BOILER;
        return REGISTRATE.blockEntity(id, MachineBlock.factory(Voltage.PRIMITIVE))
                .blockEntity()
                .simpleCapability(Boiler.builder(burnSpeed))
                .simpleCapability(StackProcessingContainer.builder(layout))
                .menu(ProcessingMenu.machine(layout))
                .plugin(MachinePlugin::noBook)
                .plugin(BoilerPlugin::new)
                .build()
                .build()
                .transform(baseMachine())
                .buildObject();
    }

    private static <S extends MachineSet.BuilderBase<?, ?, S>>
    Transformer<S> simpleLayout(Texture progress) {
        return $ -> $.layoutSet()
                .port(ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(progress, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build();
    }
}
