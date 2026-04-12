package org.shsts.tinactory.integration.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.compat.jei.category.IIngredientBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Arrays;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingJeiHelper {
    private ProcessingJeiHelper() {}

    public static void addIngredient(IIngredientBuilder builder, Layout.SlotInfo slot, IProcessingObject ingredient) {
        if (ingredient instanceof StackIngredient<?> stackIngredient && stackIngredient.type() == PortType.ITEM) {
            builder.itemInput(slot, (ItemStack) stackIngredient.stack());
        } else if (ingredient instanceof ItemsIngredient item) {
            if (item.amount <= 0) {
                builder.itemNotConsumedInput(slot, List.of(item.ingredient.getItems()));
            } else {
                var items = Arrays.stream(item.ingredient.getItems())
                    .map(stack -> StackHelper.copyWithCount(stack, item.amount))
                    .toList();
                builder.itemInput(slot, items);
            }
        } else if (
            ingredient instanceof StackIngredient<?> stackIngredient && stackIngredient.type() == PortType.FLUID
        ) {
            builder.fluidInput(slot, (FluidStack) stackIngredient.stack());
        } else if (ingredient instanceof StackResult<?> stackResult && stackResult.type() == PortType.ITEM) {
            builder.itemOutput(slot, (ItemStack) stackResult.stack(), stackResult.rate());
        } else if (ingredient instanceof StackResult<?> stackResult && stackResult.type() == PortType.FLUID) {
            builder.fluidOutput(slot, (FluidStack) stackResult.stack(), stackResult.rate());
        } else {
            throw new IllegalArgumentException("Unknown processing ingredient type %s"
                .formatted(ingredient.getClass()));
        }
    }
}
