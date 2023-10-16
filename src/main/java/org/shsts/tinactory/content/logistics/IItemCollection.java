package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

/**
 * Represent a collection of items without specific slots.
 * All these APIs ignores the stack limit of ItemStack.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IItemCollection {
    boolean acceptInput();

    boolean acceptOutput();

    ItemStack insertItem(ItemStack stack, boolean simulate);

    ItemStack extractItem(ItemStack item, boolean simulate);

    int getItemCount(ItemStack item);

    /**
     * DO NOT change the returned ItemStack.
     */
    Collection<ItemStack> getAllItems();
}
