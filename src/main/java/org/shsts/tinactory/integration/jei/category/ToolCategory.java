package org.shsts.tinactory.integration.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.AllBlocks;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.gui.layout.AllLayouts;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolCategory extends RecipeCategory<ToolRecipe> {
    public ToolCategory(RecipeType<ToolRecipe> type, IJeiHelpers helpers) {
        super(type, helpers, AllLayouts.WORKBENCH, new ItemStack(AllBlocks.WORKBENCH.get()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ToolRecipe recipe, IFocusGroup focuses) {
        var shaped = recipe.shapedRecipe;

        this.addIngredient(builder, -1, Ingredient.of(shaped.getResultItem()), RecipeIngredientRole.OUTPUT);

        var k = 0;
        for (var toolIngredient : recipe.toolIngredients) {
            this.addIngredient(builder, k, toolIngredient, RecipeIngredientRole.INPUT);
            if (++k >= 9) {
                break;
            }
        }
        for (var i = 0; i < shaped.getHeight(); i++) {
            for (var j = 0; j < shaped.getWidth(); j++) {
                var ingredient = recipe.shapedRecipe.getIngredients().get(i * shaped.getWidth() + j);
                this.addIngredient(builder, 9 + i * 3 + j, ingredient, RecipeIngredientRole.INPUT);
            }
        }
    }
}
