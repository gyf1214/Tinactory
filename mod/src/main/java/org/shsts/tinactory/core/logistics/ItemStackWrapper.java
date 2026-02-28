package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ItemStackWrapper(ItemStack stack) implements IIngredientKey {
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof ItemStackWrapper wrapper &&
            StackHelper.canItemsStack(stack, wrapper.stack));
    }

    /**
     * Serializing caps is too expensive so we only rely on item and tag.
     */
    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem(), stack.getTag());
    }
}
