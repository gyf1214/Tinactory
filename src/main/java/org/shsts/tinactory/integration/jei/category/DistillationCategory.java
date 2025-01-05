package org.shsts.tinactory.integration.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllMultiBlocks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationCategory extends ProcessingCategory<ProcessingRecipe> {
    public DistillationCategory() {
        super(AllRecipes.DISTILLATION, AllLayouts.DISTILLATION_TOWER.get(5),
            AllMultiBlocks.DISTILLATION_TOWER.get());
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
