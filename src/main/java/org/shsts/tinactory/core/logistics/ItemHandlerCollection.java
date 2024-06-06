package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.IItemCollection;

import javax.annotation.ParametersAreNonnullByDefault;
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

    public ItemHandlerCollection(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public boolean isEmpty() {
        for (var i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        var size = itemHandler.getSlots();
        for (var i = 0; i < size; i++) {
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
        return ItemHandlerHelper.insertItemStacked(itemHandler, stack.copy(), simulate);
    }

    @Override
    public ItemStack extractItem(ItemStack item, boolean simulate) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var slots = itemHandler.getSlots();
        var ret = ItemStack.EMPTY;
        var amount = item.getCount();
        for (var i = 0; i < slots; i++) {
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
        var slots = itemHandler.getSlots();
        var ret = 0;
        for (var i = 0; i < slots; i++) {
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
        var slots = itemHandler.getSlots();
        for (var i = 0; i < slots; i++) {
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
        for (var i = 0; i < wrapper.getSlots(); i++) {
            if (i < filters.size()) {
                wrapper.setFilter(i, filters.get(i));
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
        for (var i = 0; i < wrapper.getSlots(); i++) {
            wrapper.resetFilter(i);
        }
    }
}
