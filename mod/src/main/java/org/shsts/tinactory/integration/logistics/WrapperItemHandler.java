package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.shsts.tinactory.core.logistics.PortNotifier;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.shsts.tinactory.core.logistics.StackHelper.FALSE_FILTER;
import static org.shsts.tinactory.core.logistics.StackHelper.TRUE_FILTER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperItemHandler extends PortNotifier implements IItemHandlerModifiable {
    private final IItemHandlerModifiable compose;
    private final Predicate<ItemStack>[] filters;
    private final boolean[] allowOutputs;

    public WrapperItemHandler(int size) {
        this(new ItemStackHandler(size));
    }

    @SuppressWarnings("unchecked")
    public WrapperItemHandler(IItemHandlerModifiable compose) {
        var size = compose.getSlots();
        this.compose = compose;
        this.filters = new Predicate[size];
        this.allowOutputs = new boolean[size];
        Arrays.fill(filters, TRUE_FILTER);
        Arrays.fill(allowOutputs, true);
    }

    public void setFilter(int idx, Predicate<ItemStack> sth) {
        filters[idx] = sth;
    }

    public void disallowInput(int idx) {
        filters[idx] = FALSE_FILTER;
    }

    public void resetFilter(int idx) {
        filters[idx] = TRUE_FILTER;
    }

    public boolean allowOutput(int idx) {
        return allowOutputs[idx];
    }

    public void setAllowOutput(int idx, boolean value) {
        allowOutputs[idx] = value;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        compose.setStackInSlot(slot, stack);
        invokeUpdate();
    }

    @Override
    public int getSlots() {
        return compose.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return compose.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        var reminder = compose.insertItem(slot, stack, simulate);
        if (!simulate && reminder.getCount() < stack.getCount()) {
            invokeUpdate();
        }
        return reminder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowOutputs[slot]) {
            return ItemStack.EMPTY;
        }
        var extracted = compose.extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            invokeUpdate();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return compose.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return filters[slot].test(stack) && compose.isItemValid(slot, stack);
    }
}
