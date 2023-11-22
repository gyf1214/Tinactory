package org.shsts.tinactory.test;

import net.minecraft.world.item.CreativeModeTab;
import org.shsts.tinactory.content.primitive.PrimitiveSet;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

public final class AllBlocks {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> TEST_RECIPE_TYPE;

    public static final Layout TEST_FLUID_LAYOUT;
    public static final PrimitiveSet<ProcessingRecipe.Simple> TEST_SET;

    static {
        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);

        TEST_RECIPE_TYPE = REGISTRATE.simpleProcessingRecipeType("test");

        TEST_FLUID_LAYOUT = Layout.builder()
                .slot(0, 0, 0, 0, Layout.SlotType.FLUID_INPUT)
                .slot(1, ContainerMenu.SLOT_SIZE * 2, 0, 0, Layout.SlotType.FLUID_OUTPUT)
                .build();

        TEST_SET = PrimitiveSet.create(REGISTRATE, "test",
                ModelGen.gregtech("machines/alloy_smelter/overlay"),
                TEST_RECIPE_TYPE, TEST_FLUID_LAYOUT);
    }

    public static void init() {}
}
