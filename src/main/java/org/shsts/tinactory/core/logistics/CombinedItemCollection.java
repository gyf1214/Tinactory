package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedItemCollection extends CombinedCollection implements IItemCollection {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        var stack1 = stack.copy();
        for (var compose : composes) {
            if (stack1.isEmpty()) {
                break;
            }
            stack1 = compose.insertItem(stack1, simulate);
        }
        if (!simulate && stack1.getCount() < stack.getCount()) {
            invokeUpdate();
        }
        return stack1;
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
                } else if (StackHelper.canItemsStack(ret, stack)) {
                    ret.grow(stack.getCount());
                } else {
                    // don't know what to do actually, can only destroy the extracted item
                    LOGGER.warn("{}: Extracted item {} cannot stack with required item {}",
                        this, stack, ret);
                    continue;
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
}
