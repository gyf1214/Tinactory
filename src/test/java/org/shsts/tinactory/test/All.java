package org.shsts.tinactory.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.ProcessingPlugin;
import org.shsts.tinactory.content.gui.RecipeBookPlugin;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.RecipeProcessor;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.content.AllRecipes.ORE_ANALYZER;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.test.TinactoryTest.REGISTRATE;

public final class All {
    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> TEST_RECIPE_TYPE;
    public static final Layout TEST_FLUID_LAYOUT;
    public static final RegistryEntry<SimpleFluid> TEST_STEAM;
    public static final RegistryEntry<FluidCell> TEST_FLUID_CELL;
    public static final RegistryEntry<Item> TEST_ORE;
    public static final BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>> TEST_MACHINE;
    public static final BlockEntitySet<TestGenerator, MachineBlock<TestGenerator>> TEST_GENERATOR;

    public static final ResourceLocation TEST_BASE_TECH;
    public static final ResourceLocation TEST_TECH;

    static {
        TEST_RECIPE_TYPE = REGISTRATE.simpleProcessingRecipeType("test").register();

        TEST_FLUID_LAYOUT = Layout.builder()
                .port(SlotType.FLUID_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_OUTPUT)
                .slot(Menu.SLOT_SIZE * 3, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .buildLayout();

        TEST_STEAM = REGISTRATE.simpleFluid("steam", ModelGen.gregtech("blocks/fluids/fluid.steam"));

        TEST_FLUID_CELL = REGISTRATE.item("fluid_cell", properties -> new FluidCell(properties, 16000))
                .model(ModelGen.basicItem(ModelGen.gregtech("items/metaitems/fluid_cell/base")))
                .register();

        TEST_ORE = REGISTRATE.item("test_ore", Item::new)
                .model(ModelGen.basicItem(ModelGen.modLoc("items/material/raw")))
                .tint(0xFFFF6400)
                .register();

        TEST_MACHINE = REGISTRATE.blockEntitySet("machine/test", SmartBlockEntity::new,
                        MachineBlock.factory(Voltage.ULV))
                .entityClass(SmartBlockEntity.class)
                .blockEntity()
                .ticking().eventManager()
                .simpleCapability(Machine.builder(Voltage.ULV))
                .capability(StackProcessingContainer::builder)
                .layout(TEST_FLUID_LAYOUT)
                .build()
                .capability(RecipeProcessor::builder)
                .recipeType(TEST_RECIPE_TYPE)
                .voltage(Voltage.ULV)
                .build()
                .menu(ProcessingMenu.factory(TEST_FLUID_LAYOUT))
                .plugin(ProcessingPlugin::new)
                .plugin(RecipeBookPlugin.builder(TEST_RECIPE_TYPE, TEST_FLUID_LAYOUT))
                .build() // menu
                .build() // blockEntity
                .block()
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/ulv"),
                        ModelGen.gregtech("blocks/machines/alloy_smelter/overlay_front")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        TEST_GENERATOR = REGISTRATE.blockEntitySet("machine/test_generator",
                        TestGenerator.factory(Voltage.ULV, 1),
                        MachineBlock.factory(Voltage.ULV))
                .entityClass(TestGenerator.class)
                .blockEntity()
                .simpleCapability(Machine.builder(Voltage.ULV))
                .build()
                .block()
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/ulv"),
                        ModelGen.gregtech("blocks/overlay/machine/overlay_screen")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        TEST_BASE_TECH = REGISTRATE.tech("test_base")
                .maxProgress(1000)
                .createObject();

        TEST_TECH = REGISTRATE.tech("test")
                .maxProgress(2000)
                .depends(TEST_BASE_TECH)
                .createObject();
    }

    public static void init() {}

    public static void initRecipes() {
        TEST_RECIPE_TYPE.recipe("test_water")
                .inputFluid(0, Fluids.WATER, 500)
                .outputFluid(1, Fluids.WATER, 1000)
                .workTicks(50)
                .build();

        TEST_RECIPE_TYPE.recipe("test_steam")
                .inputFluid(0, All.TEST_STEAM, 500)
                .outputFluid(1, All.TEST_STEAM, 1000)
                .workTicks(50)
                .build();

        ORE_ANALYZER.recipe(REGISTRATE, "test_ore")
                .inputItem(0, AllMaterials.STONE.entry("block"), 1)
                .outputItem(1, All.TEST_ORE, 1, 0.75f)
                .workTicks(20)
                .build();
    }
}
