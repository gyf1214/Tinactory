package org.shsts.tinactory.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;
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
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(AllBlocks.TEST_FLUID_CELL.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ProcessingCategory<>(
                this.testType, registration.getJeiHelpers(), AllBlocks.TEST_FLUID_LAYOUT,
                AllBlockEntities.TEST_MACHINE.getBlock()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = ClientUtil.getRecipeManager();
        var recipes = recipeManager.getAllRecipesFor(AllRecipes.TEST_RECIPE_TYPE.get());
        registration.addRecipes(this.testType, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(AllBlockEntities.TEST_MACHINE.getBlock()), this.testType);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        var ingredientManager = jeiRuntime.getIngredientManager();
        var ingredients = ingredientManager.getAllIngredients(ForgeTypes.FLUID_STACK).stream()
                .map(fluid -> AllBlocks.TEST_FLUID_CELL.get().getFluidCell(new FluidStack(fluid, 16000)))
                .toList();
        ingredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ingredients);
    }
}
