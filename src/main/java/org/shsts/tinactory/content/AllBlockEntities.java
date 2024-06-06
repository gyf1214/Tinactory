package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.BlockTags;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.gui.BoilerPlugin;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.registrate.common.BlockEntitySet;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.machine.ProcessingSet.generator;
import static org.shsts.tinactory.content.machine.ProcessingSet.marker;
import static org.shsts.tinactory.content.machine.ProcessingSet.processing;
import static org.shsts.tinactory.content.model.ModelGen.gregtech;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final BlockEntitySet<NetworkController, MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final BlockEntitySet<SmartBlockEntity, PrimitiveBlock<SmartBlockEntity>> WORKBENCH;
    public static final ProcessingSet<ResearchRecipe> RESEARCH_TABLE;
    public static final ProcessingSet<AssemblyRecipe> ASSEMBLER;
    public static final ProcessingSet<ProcessingRecipe> STONE_GENERATOR;
    public static final ProcessingSet<OreAnalyzerRecipe> ORE_ANALYZER;
    public static final ProcessingSet<ProcessingRecipe> MACERATOR;
    public static final ProcessingSet<ProcessingRecipe> ORE_WASHER;
    public static final ProcessingSet<ProcessingRecipe> CENTRIFUGE;
    public static final ProcessingSet<ProcessingRecipe> THERMAL_CENTRIFUGE;
    public static final MachineSet ELECTRIC_FURNACE;
    public static final ProcessingSet<ProcessingRecipe> ALLOY_SMELTER;
    public static final ProcessingSet<GeneratorRecipe> STEAM_TURBINE;
    public static final BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>> LOW_PRESSURE_BOILER;
    public static final BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>> HIGH_PRESSURE_BOILER;

    static {
        PROCESSING_SETS = new HashSet<>();

        NETWORK_CONTROLLER = REGISTRATE.blockEntitySet("network/controller",
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
                .block()
                .transform(ModelGen.machine(Voltage.LV, gregtech("blocks/overlay/machine/overlay_screen")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        WORKBENCH = REGISTRATE.blockEntitySet("primitive/workbench",
                        SmartBlockEntity::new,
                        PrimitiveBlock<SmartBlockEntity>::new)
                .entityClass(SmartBlockEntity.class)
                .blockEntity()
                .simpleCapability(Workbench::builder)
                .menu(WorkbenchMenu::new).build()
                .build()
                .block()
                .transform(ModelGen.primitive(gregtech("blocks/casings/crafting_table")))
                .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        RESEARCH_TABLE = set(processing(AllRecipes.RESEARCH))
                .overlay(gregtech("blocks/overlay/machine/overlay_screen"))
                .voltage(Voltage.ULV)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_MULTIPLE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        var assembler = set(processing(AllRecipes.ASSEMBLER))
                .overlay(gregtech("blocks/machines/assembler"))
                .voltage(Voltage.ULV)
                .layoutSet()
                .port(SlotType.ITEM_INPUT);
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < (i == 2 ? 2 : 3); j++) {
                assembler.slot(SLOT_SIZE * j, 1 + SLOT_SIZE * i);
            }
        }
        ASSEMBLER = assembler.port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE * 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_CIRCUIT, 8 + SLOT_SIZE * 3, SLOT_SIZE)
                .build()
                .buildObject();

        STONE_GENERATOR = set(processing(AllRecipes.STONE_GENERATOR))
                .overlay(gregtech("blocks/machines/rock_crusher"))
                .voltage(Voltage.PRIMITIVE)
                .layoutSet()
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 2, 1 + SLOT_SIZE / 2)
                .port(SlotType.FLUID_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .progressBar(Texture.PROGRESS_MACERATE, 8, SLOT_SIZE / 2)
                .build()
                .buildObject();

        var oreAnalyzer = set(ProcessingSet.oreAnalyzer())
                .overlay(gregtech("blocks/machines/electromagnetic_separator"))
                .voltage(Voltage.PRIMITIVE)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_SIFT, 8 + SLOT_SIZE, SLOT_SIZE / 2);
        for (var i = 3; i < 6; i++) {
            var startVoltage = i == 5 ? Voltage.PRIMITIVE : Voltage.LV;
            oreAnalyzer.port(SlotType.ITEM_OUTPUT)
                    .slot(SLOT_SIZE * i, 1 + SLOT_SIZE / 2, startVoltage, Voltage.MV)
                    .slot(SLOT_SIZE * i, 1, Voltage.HV)
                    .slot(SLOT_SIZE * i, 1 + SLOT_SIZE, Voltage.HV);
        }
        ORE_ANALYZER = oreAnalyzer.build().buildObject();

        MACERATOR = set(marker(AllRecipes.MACERATOR))
                .overlay(gregtech("blocks/machines/macerator"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2, Voltage.LV, Voltage.HV)
                .slot(SLOT_SIZE * 3, 1, Voltage.EV)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2, Voltage.MV, Voltage.HV)
                .slot(SLOT_SIZE * 4, 1, Voltage.EV)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2, List.of(Voltage.HV))
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE, Voltage.EV)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE, Voltage.EV)
                .progressBar(Texture.PROGRESS_MACERATE, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ORE_WASHER = set(marker(AllRecipes.ORE_WASHER))
                .overlay(gregtech("blocks/machines/ore_washer"))
                .voltage(Voltage.PRIMITIVE)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE / 2, Voltage.ULV)
                .progressBar(Texture.PROGRESS_BATH, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        CENTRIFUGE = set(marker(AllRecipes.CENTRIFUGE))
                .overlay(gregtech("blocks/machines/centrifuge"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1).slot(SLOT_SIZE * 5, 1).slot(SLOT_SIZE * 6, 1)
                .port(SlotType.FLUID_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE).slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .slot(SLOT_SIZE * 6, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_EXTRACT, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        THERMAL_CENTRIFUGE = set(marker(AllRecipes.THERMAL_CENTRIFUGE))
                .overlay(gregtech("blocks/machines/thermal_centrifuge"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ELECTRIC_FURNACE = ProcessingSet.electricFurnace()
                .voltage(Voltage.ULV)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        ALLOY_SMELTER = set(processing(AllRecipes.ALLOY_SMELTER))
                .voltage(Voltage.ULV)
                .overlay(gregtech("blocks/machines/alloy_smelter"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE * 2, SLOT_SIZE / 2)
                .build()
                .buildObject();

        STEAM_TURBINE = set(generator(AllRecipes.STEAM_TURBINE))
                .voltage(Voltage.ULV, Voltage.HV)
                .overlay(gregtech("blocks/generators/steam_turbine/overlay_side"))
                .layoutSet()
                .port(SlotType.FLUID_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.FLUID_OUTPUT)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
                .progressBar(Texture.PROGRESS_GAS, 8 + SLOT_SIZE, SLOT_SIZE / 2)
                .build()
                .buildObject();

        LOW_PRESSURE_BOILER = boiler("low", 1d, Voltage.ULV);
        HIGH_PRESSURE_BOILER = boiler("high", 2.2d, Voltage.MV);
    }

    public static final Set<ProcessingSet<?>> PROCESSING_SETS;

    private static <T extends ProcessingRecipe> ProcessingSet.Builder<T, ?>
    set(ProcessingSet.Builder<T, ?> builder) {
        return builder.onCreateObject(PROCESSING_SETS::add);
    }

    private static BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
    boiler(String name, double burnSpeed, Voltage casingVoltage) {
        var id = "machine/boiler/" + name;
        var layout = AllLayouts.BOILER;
        return REGISTRATE.blockEntitySet(id, SmartBlockEntity::new, MachineBlock.factory(Voltage.PRIMITIVE))
                .entityClass(SmartBlockEntity.class)
                .blockEntity()
                .eventManager()
                .simpleCapability(Machine::builder)
                .simpleCapability(Boiler.builder(burnSpeed))
                .simpleCapability(StackProcessingContainer.builder(layout))
                .menu(ProcessingMenu.factory(layout))
                .plugin(MachinePlugin.noBook(layout))
                .plugin(BoilerPlugin::new)
                .build()
                .build()
                .block()
                .transform(ModelGen.machine(casingVoltage, gregtech("blocks/generators/boiler/coal")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();
    }

    public static void init() {}
}
