package org.shsts.tinactory.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import static org.shsts.tinactory.content.AllRecipes.ORE_ANALYZER;
import static org.shsts.tinactory.test.TinactoryTest.REGISTRATE;

public final class AllRecipes {
    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> TEST_RECIPE_TYPE;

    static {
        TEST_RECIPE_TYPE = REGISTRATE.simpleProcessingRecipeType("test");
    }

    public static void init() {}

    public static void initRecipes() {
        TEST_RECIPE_TYPE.modRecipe(TinactoryTest.modLoc("test_water"))
                .inputFluid(0, Fluids.WATER, 500)
                .outputFluid(1, Fluids.WATER, 1000)
                .workTicks(50)
                .build();

        TEST_RECIPE_TYPE.modRecipe(TinactoryTest.modLoc("test_steam"))
                .inputFluid(0, AllBlocks.TEST_STEAM, 500)
                .outputFluid(1, AllBlocks.TEST_STEAM, 1000)
                .workTicks(50)
                .build();

        ORE_ANALYZER.recipe(REGISTRATE, new ResourceLocation(TinactoryTest.ID, "test_ore"))
                .inputItem(0, Items.COBBLESTONE, 1)
                .outputItem(1, AllBlocks.TEST_ORE, 1, 0.75f)
                .workTicks(20)
                .build();
    }
}
