package org.shsts.tinactory.core.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedItemCollection implements IItemCollection {
    private final List<IItemCollection> composes = new ArrayList<>();
    @Nullable
    private Runnable updateListener = null;

    public void setComposes(Collection<IItemCollection> val) {
        composes.clear();
        composes.addAll(val);
    }

    public void onUpdate(Runnable listener) {
        updateListener = listener;
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        return composes.stream().anyMatch($ -> $.acceptInput(stack));
    }

    @Override
    public boolean acceptOutput() {
        return composes.stream().anyMatch(IPort::acceptOutput);
    }

    private void invokeUpdate() {
        if (updateListener != null) {
            updateListener.run();
        }
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        var count = stack.getCount();
        for (var compose : composes) {
            stack = compose.insertItem(stack, simulate);
            if (stack.isEmpty()) {
                break;
            }
        }
        if (!simulate && stack.getCount() < count) {
            invokeUpdate();
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(ItemStack item, boolean simulate) {
        var item1 = item.copy();
        var ret = ItemStack.EMPTY;
        for (var compose : composes) {
            if (item1.isEmpty()) {
                break;
            }
            var stack = compose.extractItem(item1, simulate);
            if (!stack.isEmpty()) {
                if (ret.isEmpty()) {
                    ret = stack;
                }
                item1.shrink(stack.getCount());
            }
        }
        if (!simulate && !ret.isEmpty()) {
            invokeUpdate();
        }
        return ret;
    }

    @Override
    public ItemStack extractItem(int limit, boolean simulate) {
        var ret = composes.isEmpty() ? ItemStack.EMPTY :
            composes.get(0).extractItem(limit, simulate);
        if (!simulate && !ret.isEmpty()) {
            invokeUpdate();
        }
        return ret;
    }

    @Override
    public int getItemCount(ItemStack item) {
        return composes.stream().mapToInt($ -> $.getItemCount(item)).sum();
    }

    @Override
    public Collection<ItemStack> getAllItems() {
        return composes.stream().flatMap($ -> $.getAllItems().stream()).toList();
    }

    @Override
    public void setItemFilter(List<? extends Predicate<ItemStack>> filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetItemFilter() {
        throw new UnsupportedOperationException();
    }
}
