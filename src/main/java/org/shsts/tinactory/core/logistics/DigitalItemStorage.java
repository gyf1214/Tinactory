package org.shsts.tinactory.core.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IItemFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.ITEM_COLLECTION;
import static org.shsts.tinactory.core.logistics.StackHelper.TRUE_FILTER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalItemStorage extends DigitalStorage
    implements IItemCollection, IItemFilter, INBTSerializable<CompoundTag> {
    private record ItemStackWrapper(ItemStack stack) {
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

    private final Map<ItemStackWrapper, ItemStack> items = new HashMap<>();
    private Predicate<ItemStack> filter = TRUE_FILTER;

    public DigitalItemStorage(int bytesLimit) {
        super(bytesLimit);
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (!filter.test(stack)) {
            return false;
        }
        if (bytesRemaining < CONFIG.bytesPerItem.get()) {
            return false;
        }
        if (!items.containsKey(new ItemStackWrapper(stack))) {
            return bytesRemaining >= CONFIG.bytesPerItem.get() + CONFIG.bytesPerItemType.get();
        }
        return true;
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !acceptInput(stack)) {
            return stack;
        }
        var key = new ItemStackWrapper(stack);
        var bytesPerItem = CONFIG.bytesPerItem.get();
        if (!items.containsKey(key)) {
            var newBytesRemaining = bytesRemaining - CONFIG.bytesPerItemType.get();
            var inserted = Math.min(stack.getCount(), newBytesRemaining / bytesPerItem);
            assert inserted > 0 && inserted <= stack.getCount();
            var remaining = StackHelper.copyWithCount(stack, stack.getCount() - inserted);
            if (!simulate) {
                var insertedStack = StackHelper.copyWithCount(stack, inserted);
                items.put(new ItemStackWrapper(insertedStack), insertedStack);
                bytesRemaining = newBytesRemaining - inserted * bytesPerItem;
            }
            return remaining;
        } else {
            var inserted = Math.min(stack.getCount(), bytesRemaining / bytesPerItem);
            assert inserted > 0 && inserted <= stack.getCount();
            var remaining = StackHelper.copyWithCount(stack, stack.getCount() - inserted);
            if (!simulate) {
                items.get(key).grow(inserted);
                bytesRemaining -= inserted * bytesPerItem;
            }
            return remaining;
        }
    }

    @Override
    public ItemStack extractItem(ItemStack item, boolean simulate) {
        if (item.isEmpty() || !acceptOutput()) {
            return ItemStack.EMPTY;
        }
        var key = new ItemStackWrapper(item);
        if (!items.containsKey(key)) {
            return ItemStack.EMPTY;
        }
        var stack = items.get(key);
        var bytesPerItem = CONFIG.bytesPerItem.get();
        if (item.getCount() >= stack.getCount()) {
            if (!simulate) {
                items.remove(key);
                bytesRemaining += CONFIG.bytesPerItemType.get() + bytesPerItem * stack.getCount();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(item.getCount());
                bytesRemaining += bytesPerItem * item.getCount();
            }
            return item.copy();
        }
    }

    @Override
    public ItemStack extractItem(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput() || items.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var stack = items.values().iterator().next();
        var bytesPerItem = CONFIG.bytesPerItem.get();
        if (limit >= stack.getCount()) {
            if (!simulate) {
                items.remove(new ItemStackWrapper(stack));
                bytesRemaining += CONFIG.bytesPerItemType.get() + bytesPerItem * stack.getCount();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(limit);
                bytesRemaining += bytesPerItem * limit;
            }
            return StackHelper.copyWithCount(stack, limit);
        }
    }

    @Override
    public int getItemCount(ItemStack item) {
        if (!acceptOutput()) {
            return 0;
        }
        var stack = items.get(new ItemStackWrapper(item));
        return stack == null ? 0 : stack.getCount();
    }

    @Override
    public Collection<ItemStack> getAllItems() {
        return acceptOutput() ? items.values() : Collections.emptyList();
    }

    @Override
    public void setFilters(List<? extends Predicate<ItemStack>> filters) {
        filter = stack -> filters.stream().anyMatch($ -> $.test(stack));
    }

    @Override
    public void resetFilters() {
        filter = TRUE_FILTER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_COLLECTION.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var listTag = new ListTag();
        for (var stack : items.values()) {
            var itemTag = StackHelper.serializeItemStack(stack);
            listTag.add(itemTag);
        }
        tag.put("Items", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        items.clear();
        bytesRemaining = bytesLimit;

        var listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        var bytesPerItem = CONFIG.bytesPerItem.get();
        var bytesPerType = CONFIG.bytesPerItemType.get();
        for (var itemTag : listTag) {
            var stack = StackHelper.deserializeItemStack((CompoundTag) itemTag);
            items.put(new ItemStackWrapper(stack), stack);
            bytesRemaining -= bytesPerType + bytesPerItem * stack.getCount();
        }
    }
}
