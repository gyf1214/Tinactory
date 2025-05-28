package org.shsts.tinactory.core.logistics;

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

    public void setComposes(Collection<IItemCollection> val) {
        composes.clear();
        composes.addAll(val);
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        return composes.stream().anyMatch($ -> $.acceptInput(stack));
    }

    @Override
    public boolean acceptOutput() {
        return composes.stream().anyMatch(IPort::acceptOutput);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        for (var compose : composes) {
            stack = compose.insertItem(stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(ItemStack item, boolean simulate) {
        var item1 = item.copy();
        var ret = ItemStack.EMPTY;
        for (var compose : composes) {
            if (item1.isEmpty()) {
                return ret;
            }
            var stack = compose.extractItem(item1, simulate);
            if (!stack.isEmpty()) {
                if (ret.isEmpty()) {
                    ret = stack;
                }
                item1.shrink(stack.getCount());
            }
        }
        return ret;
    }

    @Override
    public ItemStack extractItem(int limit, boolean simulate) {
        return composes.isEmpty() ? ItemStack.EMPTY :
            composes.get(0).extractItem(limit, simulate);
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
