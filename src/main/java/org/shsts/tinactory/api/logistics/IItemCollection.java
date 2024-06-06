package org.shsts.tinactory.api.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represent a collection of items without specific slots.
 * All these APIs ignores the stack limit of ItemStack.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IItemCollection extends IPort {
    @Override
    default PortType type() {
        return PortType.ITEM;
    }

    boolean acceptInput(ItemStack stack);

    boolean acceptOutput();

    /**
     * returns the remaining items not inserted
     */
    ItemStack insertItem(ItemStack stack, boolean simulate);

    /**
     * returns the items taken
     */
    ItemStack extractItem(ItemStack item, boolean simulate);

    int getItemCount(ItemStack item);

    /**
     * DO NOT change the returned ItemStack.
     */
    Collection<ItemStack> getAllItems();

    void setItemFilter(List<? extends Predicate<ItemStack>> filters);

    void resetItemFilter();
}
