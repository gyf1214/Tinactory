package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperItemHandler implements IItemHandlerModifiable {
    protected final IItemHandlerModifiable compose;
    protected final List<Runnable> updateListener = new ArrayList<>();
    public boolean allowInput = true;
    public boolean allowOutput = true;
    public Predicate<ItemStack> filter = $ -> true;

    public WrapperItemHandler(int size) {
        this(new ItemStackHandler(size));
    }

    public WrapperItemHandler(IItemHandlerModifiable compose) {
        this.compose = compose;
    }

    public WrapperItemHandler(Container inv) {
        this(new InvWrapper(inv));
    }

    public void resetFilter() {
        filter = $ -> true;
    }

    public void onUpdate(Runnable cons) {
        updateListener.add(cons);
    }

    protected void invokeUpdate() {
        for (var cons : updateListener) {
            cons.run();
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
        return allowInput && filter.test(stack) && compose.isItemValid(slot, stack);
    }
}
