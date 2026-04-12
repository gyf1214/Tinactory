package org.shsts.tinactory.integration.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.compat.jei.category.IIngredientBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Arrays;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingJeiHelper {
    private ProcessingJeiHelper() {}

    public static void addIngredient(IIngredientBuilder builder, Layout.SlotInfo slot, IProcessingObject ingredient) {
        if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            builder.itemInput(slot, item.stack());
        } else if (ingredient instanceof ItemsIngredient item) {
            if (item.amount <= 0) {
                builder.itemNotConsumedInput(slot, List.of(item.ingredient.getItems()));
            } else {
                var items = Arrays.stream(item.ingredient.getItems())
                    .map(stack -> StackHelper.copyWithCount(stack, item.amount))
                    .toList();
                builder.itemInput(slot, items);
            }
        } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
            builder.fluidInput(slot, fluid.fluid());
        } else if (ingredient instanceof ProcessingResults.ItemResult item) {
            builder.itemOutput(slot, item.stack, item.rate);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluid) {
            builder.fluidOutput(slot, fluid.stack, fluid.rate);
        } else {
            throw new IllegalArgumentException("Unknown processing ingredient type %s"
                .formatted(ingredient.getClass()));
        }
    }
}
