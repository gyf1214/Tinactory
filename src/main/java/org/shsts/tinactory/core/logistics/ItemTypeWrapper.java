package org.shsts.tinactory.core.logistics;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Used to represent unique item type used in IItemCollection.
 * Two item stacks are equal if they have same item, NBT and caps.
 */
public record ItemTypeWrapper(@Nonnull ItemStack stack) {
    @Override
    public String toString() {
        return "ItemTypeWrapper{%s}".formatted(this.stack.getItem());
    }

    @Override
    public int hashCode() {
        // unfortunately capNBT is protected
        return Objects.hash(this.stack.getItem(), this.stack.getTag());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ItemTypeWrapper o && this.canStackWith(o.stack));
    }

    public boolean canStackWith(@Nonnull ItemStack stack) {
        return ItemHelper.canItemsStack(this.stack, stack);
    }
}
