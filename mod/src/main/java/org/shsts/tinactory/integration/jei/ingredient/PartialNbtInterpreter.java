package org.shsts.tinactory.integration.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

/**
 * Used to distinguish item by NBT in ingredient list but not in recipe.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PartialNbtInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    public static final PartialNbtInterpreter INSTANCE = new PartialNbtInterpreter();

    @Override
    public String apply(ItemStack stack, UidContext context) {
        if (context == UidContext.Recipe) {
            return NONE;
        }
        var tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return NONE;
        }
        return tag.toString();
    }
}
