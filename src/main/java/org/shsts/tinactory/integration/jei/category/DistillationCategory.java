package org.shsts.tinactory.integration.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationCategory extends ProcessingCategory<ProcessingRecipe> {
    public DistillationCategory(
        IRecipeType<? extends IRecipeBuilderBase<ProcessingRecipe>> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected void setRecipe(ProcessingRecipe recipe, IIngredientBuilder builder) {
        var inputs = layout.getProcessingInputs(recipe);
        for (var input : inputs) {
            addIngredient(builder, input.slot(), input.val());
        }
        var slots = Math.min(recipe.outputs.size(), 6);
        for (var i = 0; i < slots; i++) {
            var output = recipe.outputs.get(i);
            if (output.port() == 1) {
                addIngredient(builder, layout.slots.get(1 + i), output.result());
            } else if (output.port() == 2) {
                addIngredient(builder, layout.slots.get(7 + i), output.result());
            }
        }
    }
}
