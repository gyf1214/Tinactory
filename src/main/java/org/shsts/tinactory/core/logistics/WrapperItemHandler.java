package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperItemHandler implements IItemHandlerModifiable {
    private static final Predicate<ItemStack> TRUE = $ -> true;

    private final IItemHandlerModifiable compose;
    @Nullable
    private Runnable updateListener = null;
    public boolean allowInput = true;
    public boolean allowOutput = true;
    private final List<Predicate<ItemStack>> filters;

    public WrapperItemHandler(int size) {
        this(new ItemStackHandler(size));
    }

    public WrapperItemHandler(IItemHandlerModifiable compose) {
        this.compose = compose;
        this.filters = new ArrayList<>(Collections.nCopies(compose.getSlots(), TRUE));
    }

    public WrapperItemHandler(Container inv) {
        this(new InvWrapper(inv));
    }

    public void setFilter(int idx, Predicate<ItemStack> sth) {
        filters.set(idx, sth);
    }

    public void resetFilter(int idx) {
        filters.set(idx, TRUE);
    }

    public void onUpdate(Runnable cons) {
        updateListener = cons;
    }

    public void resetOnUpdate() {
        updateListener = null;
    }

    private void invokeUpdate() {
        if (updateListener != null) {
            updateListener.run();
        }
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

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return compose.getStackInSlot(slot);
    }

    @Nonnull
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

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowOutput) {
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
        return allowInput && filters.get(slot).test(stack) && compose.isItemValid(slot, stack);
    }
}
