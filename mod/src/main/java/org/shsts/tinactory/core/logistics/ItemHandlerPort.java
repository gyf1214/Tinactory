package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.ILimitedPort;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Item Collection backed by ItemHandler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemHandlerPort implements IItemPort, IPortNotifier, ILimitedPort {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final IItemHandler itemHandler;
    @Nullable
    private final IPortNotifier notifier;
    private final int minSlot;
    private final int maxSlot;
    private final RangedWrapper rangedWrapper;

    public ItemHandlerPort(IItemHandlerModifiable itemHandler, int minSlot, int maxSlot) {
        this.itemHandler = itemHandler;
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;
        this.rangedWrapper = new RangedWrapper(itemHandler, minSlot, maxSlot);

        if (itemHandler instanceof IPortNotifier notifier1) {
            this.notifier = notifier1;
        } else {
            this.notifier = null;
        }
    }

    public ItemHandlerPort(IItemHandlerModifiable itemHandler) {
        this(itemHandler, 0, itemHandler.getSlots());
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
        // if the inner handler is not a wrapper, we don't know whether it accepts output
        if (!(itemHandler instanceof WrapperItemHandler wrapper)) {
            return true;
        }
        for (var i = minSlot; i < maxSlot; i++) {
            if (wrapper.allowOutput(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        // need to make sure stack is not set to some itemHandler
        return ItemHandlerHelper.insertItemStacked(rangedWrapper, stack.copy(), simulate);
    }

    @Override
    public ItemStack extract(ItemStack item, boolean simulate) {
        if (item.isEmpty() || !acceptOutput()) {
            return ItemStack.EMPTY;
        }
        var ret = ItemStack.EMPTY;
        var amount = item.getCount();
        for (var i = minSlot; i < maxSlot; i++) {
            if (amount <= 0) {
                break;
            }
            var slotItem = itemHandler.getStackInSlot(i);
            if (StackHelper.canItemsStack(item, slotItem)) {
                var extractedItem = itemHandler.extractItem(i, amount, simulate);
                if (extractedItem.isEmpty()) {
                    continue;
                }
                if (ret.isEmpty()) {
                    ret = extractedItem;
                } else if (StackHelper.canItemsStack(ret, extractedItem)) {
                    ret.grow(extractedItem.getCount());
                } else {
                    // don't know what to do actually, can only destroy the extracted item
                    LOGGER.warn("{}: Extracted item {} cannot stack with required item {}",
                        this, extractedItem, ret);
                    continue;
                }
                amount -= extractedItem.getCount();
            }
        }
        return ret;
    }

    @Override
    public ItemStack extract(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput()) {
            return ItemStack.EMPTY;
        }
        for (var i = minSlot; i < maxSlot; i++) {
            var slotItem = itemHandler.getStackInSlot(i);
            if (!slotItem.isEmpty()) {
                return extract(StackHelper.copyWithCount(slotItem, limit), simulate);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getStorageAmount(ItemStack item) {
        if (item.isEmpty() || !acceptOutput()) {
            return 0;
        }
        var ret = 0;
        for (var i = minSlot; i < maxSlot; i++) {
            var slotItem = itemHandler.getStackInSlot(i);
            if (StackHelper.canItemsStack(item, slotItem)) {
                ret += slotItem.getCount();
            }
        }
        return ret;
    }

    @Override
    public Collection<ItemStack> getAllStorages() {
        if (!acceptOutput()) {
            return Collections.emptyList();
        }
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
    public void setFilters(List<? extends Predicate<ItemStack>> filters) {
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
    public void resetFilters() {
        if (!(itemHandler instanceof WrapperItemHandler wrapper)) {
            return;
        }
        for (var i = minSlot; i < maxSlot; i++) {
            wrapper.resetFilter(i);
        }
    }

    @Override
    public void onUpdate(Runnable listener) {
        if (notifier != null) {
            notifier.onUpdate(listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void unregisterListener(Runnable listener) {
        if (notifier != null) {
            notifier.unregisterListener(listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int getPortLimit() {
        return maxSlot - minSlot;
    }
}
