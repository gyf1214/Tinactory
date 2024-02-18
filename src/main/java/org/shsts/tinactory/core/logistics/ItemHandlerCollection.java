package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.shsts.tinactory.api.logistics.IItemCollection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Item Collection backed by ItemHandler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemHandlerCollection implements IItemCollection {
    public static final ItemHandlerCollection EMPTY = new ItemHandlerCollection(EmptyHandler.INSTANCE);

    public final IItemHandler itemHandler;

    public ItemHandlerCollection(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        var size = this.itemHandler.getSlots();
        for (var i = 0; i < size; i++) {
            if (this.itemHandler.isItemValid(i, stack)) {
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
        return ItemHandlerHelper.insertItemStacked(this.itemHandler, stack, simulate);
    }

    @Override
    public ItemStack extractItem(ItemStack item, boolean simulate) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var slots = this.itemHandler.getSlots();
        var ret = ItemStack.EMPTY;
        var amount = item.getCount();
        for (var i = 0; i < slots; i++) {
            if (amount <= 0) {
                break;
            }
            var slotItem = this.itemHandler.getStackInSlot(i);
            if (ItemHelper.canItemsStack(item, slotItem)) {
                var extractedItem = this.itemHandler.extractItem(i, amount, simulate);
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
        var slots = this.itemHandler.getSlots();
        var ret = 0;
        for (var i = 0; i < slots; i++) {
            var slotItem = this.itemHandler.getStackInSlot(i);
            if (ItemHelper.canItemsStack(item, slotItem)) {
                ret += slotItem.getCount();
            }
        }
        return ret;
    }

    @Override
    public Collection<ItemStack> getAllItems() {
        Map<ItemTypeWrapper, ItemStack> allItems = new HashMap<>();

        var slots = this.itemHandler.getSlots();
        for (var i = 0; i < slots; i++) {
            var slotItem = this.itemHandler.getStackInSlot(i);
            if (slotItem.isEmpty()) {
                continue;
            }
            var wrapper = new ItemTypeWrapper(slotItem);
            var existingItem = allItems.get(wrapper);
            if (existingItem != null) {
                existingItem.grow(slotItem.getCount());
            } else {
                allItems.put(wrapper, slotItem.copy());
            }
        }

        // clean reference to the original itemStack
        return allItems.values().stream().toList();
    }
}
