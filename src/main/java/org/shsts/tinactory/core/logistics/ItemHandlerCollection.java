package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.shsts.tinactory.api.logistics.IItemCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Item Collection backed by ItemHandler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemHandlerCollection implements IItemCollection {
    public final IItemHandler itemHandler;
    private final int minSlot;
    private final int maxSlot;
    private final RangedWrapper rangedWrapper;

    public ItemHandlerCollection(IItemHandlerModifiable itemHandler, int minSlot, int maxSlot) {
        this.itemHandler = itemHandler;
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;
        this.rangedWrapper = new RangedWrapper(itemHandler, minSlot, maxSlot);
    }

    public ItemHandlerCollection(IItemHandlerModifiable itemHandler) {
        this(itemHandler, 0, itemHandler.getSlots());
    }

    @Override
    public boolean isEmpty() {
        for (var i = minSlot; i < maxSlot; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        for (var i = minSlot; i < maxSlot; i++) {
            if (itemHandler.isItemValid(i, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        // need to make sure stack is not set to some itemHandler
        return ItemHandlerHelper.insertItemStacked(rangedWrapper, stack.copy(), simulate);
    }

    @Override
    public ItemStack extractItem(ItemStack item, boolean simulate) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var ret = ItemStack.EMPTY;
        var amount = item.getCount();
        for (var i = minSlot; i < maxSlot; i++) {
            if (amount <= 0) {
                break;
            }
            var slotItem = itemHandler.getStackInSlot(i);
            if (ItemHelper.canItemsStack(item, slotItem)) {
                var extractedItem = itemHandler.extractItem(i, amount, simulate);
                if (extractedItem.isEmpty()) {
                    continue;
                }
                if (ret.isEmpty()) {
                    ret = extractedItem;
                } else if (ItemHelper.canItemsStack(ret, extractedItem)) {
                    ret.grow(extractedItem.getCount());
                } else {
                    // don't know what to do actually, can only destroy the extracted item
                    continue;
                }
                amount -= extractedItem.getCount();
            }
        }
        return ret;
    }

    @Override
    public int getItemCount(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }
        var ret = 0;
        for (var i = minSlot; i < maxSlot; i++) {
            var slotItem = itemHandler.getStackInSlot(i);
            if (ItemHelper.canItemsStack(item, slotItem)) {
                ret += slotItem.getCount();
            }
        }
        return ret;
    }

    @Override
    public Collection<ItemStack> getAllItems() {
        var allItems = new ArrayList<ItemStack>();
        for (var i = minSlot; i < maxSlot; i++) {
            var slotItem = itemHandler.getStackInSlot(i);
            if (!slotItem.isEmpty()) {
                allItems.add(slotItem);
            }
        }
        return allItems;
    }

    @Override
    public void setItemFilter(List<? extends Predicate<ItemStack>> filters) {
        if (!(itemHandler instanceof WrapperItemHandler wrapper)) {
            return;
        }
        for (var i = minSlot; i < maxSlot; i++) {
            if (i - minSlot < filters.size()) {
                wrapper.setFilter(i, filters.get(i - minSlot));
            } else {
                wrapper.setFilter(i, $ -> false);
            }
        }
    }

    @Override
    public void resetItemFilter() {
        if (!(itemHandler instanceof WrapperItemHandler wrapper)) {
            return;
        }
        for (var i = minSlot; i < maxSlot; i++) {
            wrapper.resetFilter(i);
        }
    }
}
