package org.shsts.tinactory.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TinactoryTest.ID, "jei");
    }

    private final RecipeType<ProcessingRecipe.Simple> testType =
            new RecipeType<>(TinactoryTest.modLoc("jei/category/test"), ProcessingRecipe.Simple.class);

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ProcessingCategory<>(
                this.testType, registration.getJeiHelpers(), AllBlocks.TEST_FLUID_LAYOUT,
                AllBlockEntities.TEST_MACHINE.getBlock()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var connection = Minecraft.getInstance().getConnection();
        assert connection != null;
        var recipeManager = connection.getRecipeManager();
        var recipes = recipeManager.getAllRecipesFor(AllBlocks.TEST_RECIPE_TYPE.get());
        registration.addRecipes(this.testType, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(AllBlockEntities.TEST_MACHINE.getBlock()), this.testType);
    }
}
