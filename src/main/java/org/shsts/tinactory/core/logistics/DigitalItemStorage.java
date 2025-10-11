package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IItemFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.core.logistics.StackHelper.TRUE_FILTER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalItemStorage extends PortNotifier implements IItemCollection,
    IItemFilter, INBTSerializable<CompoundTag> {
    private final IDigitalProvider provider;
    private final Map<ItemStackWrapper, ItemStack> items = new HashMap<>();
    private Predicate<ItemStack> filter = TRUE_FILTER;

    public DigitalItemStorage(IDigitalProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (!filter.test(stack) || !provider.canConsume(CONFIG.bytesPerItem.get())) {
            return false;
        }
        if (!items.containsKey(new ItemStackWrapper(stack))) {
            return provider.canConsume(CONFIG.bytesPerItem.get() + CONFIG.bytesPerItemType.get());
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
            provider.consume(CONFIG.bytesPerItemType.get());
            var inserted = Math.min(stack.getCount(), provider.consumeLimit(bytesPerItem));
            assert inserted > 0 && inserted <= stack.getCount();
            var remaining = StackHelper.copyWithCount(stack, stack.getCount() - inserted);
            if (!simulate) {
                var insertedStack = StackHelper.copyWithCount(stack, inserted);
                items.put(new ItemStackWrapper(insertedStack), insertedStack);
                provider.consume(inserted * bytesPerItem);
                invokeUpdate();
            }
            return remaining;
        } else {
            var inserted = Math.min(stack.getCount(), provider.consumeLimit(bytesPerItem));
            assert inserted > 0 && inserted <= stack.getCount();
            var remaining = StackHelper.copyWithCount(stack, stack.getCount() - inserted);
            if (!simulate) {
                items.get(key).grow(inserted);
                provider.consume(inserted * bytesPerItem);
                invokeUpdate();
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
                provider.restore(CONFIG.bytesPerItemType.get() + bytesPerItem * stack.getCount());
                invokeUpdate();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(item.getCount());
                provider.restore(bytesPerItem * item.getCount());
                invokeUpdate();
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
                provider.restore(CONFIG.bytesPerItemType.get() + bytesPerItem * stack.getCount());
                invokeUpdate();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(limit);
                provider.restore(bytesPerItem * limit);
                invokeUpdate();
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
        provider.reset();
        var listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        var bytesPerItem = CONFIG.bytesPerItem.get();
        var bytesPerType = CONFIG.bytesPerItemType.get();
        for (var itemTag : listTag) {
            var stack = StackHelper.deserializeItemStack((CompoundTag) itemTag);
            items.put(new ItemStackWrapper(stack), stack);
            provider.consume(bytesPerType + bytesPerItem * stack.getCount());
        }
    }
}
