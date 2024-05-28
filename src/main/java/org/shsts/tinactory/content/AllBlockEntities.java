package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.model.ModelGen.gregtech;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final BlockEntitySet<NetworkController, MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final BlockEntitySet<SmartBlockEntity, PrimitiveBlock<SmartBlockEntity>> WORKBENCH;
    public static final MachineSet<ResearchRecipe> RESEARCH_TABLE;
    public static final MachineSet<AssemblyRecipe> ASSEMBLER;
    public static final MachineSet<ProcessingRecipe> STONE_GENERATOR;
    public static final MachineSet<AssemblyRecipe> ORE_ANALYZER;
    public static final MachineSet<ProcessingRecipe> MACERATOR;
    public static final MachineSet<ProcessingRecipe> ORE_WASHER;
    public static final MachineSet<ProcessingRecipe> CENTRIFUGE;
    public static final MachineSet<ProcessingRecipe> THERMAL_CENTRIFUGE;
    public static final MachineSet<ProcessingRecipe> ALLOY_SMELTER;

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

        RESEARCH_TABLE = builder(AllRecipes.RESEARCH)
                .overlay(gregtech("blocks/overlay/machine/overlay_screen"))
                .voltage(Voltage.ULV)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build()
                .buildObject();

        var assembler = builder(AllRecipes.ASSEMBLER)
                .overlay(gregtech("blocks/machines/assembler"))
                .voltage(Voltage.ULV)
                .layoutSet()
                .port(SlotType.ITEM_INPUT);
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                assembler.slot(SLOT_SIZE * j, SLOT_SIZE * i + 1);
            }
        }
        ASSEMBLER = assembler.port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE * 2, SLOT_SIZE * 3 + 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE * 3, SLOT_SIZE)
                .build()
                .buildObject();

        STONE_GENERATOR = builder(AllRecipes.STONE_GENERATOR)
                .overlay(gregtech("blocks/machines/rock_crusher"))
                .voltage(Voltage.PRIMITIVE)
                .layoutSet()
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 2, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8, 0)
                .build()
                .buildObject();

        ORE_ANALYZER = builder(AllRecipes.ORE_ANALYZER)
                .overlay(gregtech("blocks/machines/electromagnetic_separator"))
                .voltage(Voltage.PRIMITIVE)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1)
                .slot(SLOT_SIZE * 4, 1, Voltage.LV)
                .slot(SLOT_SIZE * 5, 1, Voltage.HV)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build()
                .buildObject();

        MACERATOR = builder(AllRecipes.MACERATOR)
                .overlay(gregtech("blocks/machines/macerator"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1)
                .slot(SLOT_SIZE * 4, 1, Voltage.HV)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build()
                .buildObject();

        ORE_WASHER = builder(AllRecipes.ORE_WASHER)
                .overlay(gregtech("blocks/machines/ore_washer"))
                .voltage(Voltage.PRIMITIVE)
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5, 1, Voltage.LV)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 6, 1, Voltage.HV)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE * 2, 0)
                .build()
                .buildObject();

        CENTRIFUGE = builder(AllRecipes.CENTRIFUGE)
                .overlay(gregtech("blocks/machines/centrifuge"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1).slot(SLOT_SIZE * 4, 1).slot(SLOT_SIZE * 5, 1)
                .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE, Voltage.HV)
                .slot(SLOT_SIZE * 4, 1 + SLOT_SIZE, Voltage.HV)
                .slot(SLOT_SIZE * 5, 1 + SLOT_SIZE, Voltage.HV)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build()
                .buildObject();

        THERMAL_CENTRIFUGE = builder(AllRecipes.THERMAL_CENTRIFUGE)
                .overlay(gregtech("blocks/machines/thermal_centrifuge"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1)
                .slot(SLOT_SIZE * 4, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build()
                .buildObject();

        ALLOY_SMELTER = builder(AllRecipes.ALLOY_SMELTER)
                .overlay(gregtech("blocks/machines/alloy_smelter"))
                .layoutSet()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .slot(SLOT_SIZE, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE * 2, 0)
                .build()
                .buildObject();
    }

    public static final Set<MachineSet<?>> PROCESSING_SETS;

    private static <T extends ProcessingRecipe> MachineSet.Builder<T, ?>
    builder(RecipeTypeEntry<T, ?> recipeType) {
        return (new MachineSet.Builder<>(recipeType, Unit.INSTANCE))
                .onCreateObject(PROCESSING_SETS::add);
    }

    public static void init() {}
}
