package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperItemHandler implements IItemHandlerModifiable {
    protected final IItemHandlerModifiable compose;
    protected final List<Runnable> updateListener = new ArrayList<>();
    public boolean allowInput = true;
    public boolean allowOutput = true;

    public WrapperItemHandler(int size) {
        this(new ItemStackHandler(size));
    }

    public WrapperItemHandler(IItemHandlerModifiable compose) {
        this.compose = compose;
    }

    public WrapperItemHandler(Container inv) {
        this(new InvWrapper(inv));
    }

    public void onUpdate(Runnable cons) {
        this.updateListener.add(cons);
    }

    protected void invokeUpdate() {
        for (var cons : this.updateListener) {
            cons.run();
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.compose.setStackInSlot(slot, stack);
        this.invokeUpdate();
    }

    @Override
    public int getSlots() {
        return this.compose.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.compose.getStackInSlot(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!this.allowInput) {
            return stack;
        }
        var reminder = this.compose.insertItem(slot, stack, simulate);
        if (!simulate && reminder.getCount() < stack.getCount()) {
            this.invokeUpdate();
        }
        return reminder;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!this.allowOutput) {
            return ItemStack.EMPTY;
        }
        var extracted = this.compose.extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            this.invokeUpdate();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.compose.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.allowInput && this.compose.isItemValid(slot, stack);
    }
}
