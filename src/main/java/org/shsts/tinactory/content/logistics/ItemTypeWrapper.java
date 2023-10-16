package org.shsts.tinactory.content.logistics;

import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Used to represent unique item type used in IItemCollection.
 * Two item stacks are equal if they have same item, NBT and caps.
 */
@ParametersAreNonnullByDefault
public class ItemTypeWrapper {
    public final ItemStack stack;

    public ItemTypeWrapper(ItemStack stack) {
        assert !stack.isEmpty();
        this.stack = stack;
    }

    @Override
    public int hashCode() {
        // unfortunately capNBT is protected
        return Objects.hash(this.stack.getItem(), this.stack.getTag());
    }

    @Override
    public String toString() {
        return "ItemTypeWrapper{%s}".formatted(this.stack.getItem());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemTypeWrapper o && canItemsStack(this.stack, o.stack);
    }

    public boolean canStackWith(ItemStack stack) {
        return canItemsStack(this.stack, stack);
    }

    /**
     * This also ignores the stack limit. This means ItemStack with exact same NBT can also stack.
     */
    public static boolean canItemsStack(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.sameItem(b) && a.hasTag() == b.hasTag() &&
                (!a.hasTag() || Objects.equals(a.getTag(), b.getTag())) &&
                a.areCapsCompatible(b);
    }
}
