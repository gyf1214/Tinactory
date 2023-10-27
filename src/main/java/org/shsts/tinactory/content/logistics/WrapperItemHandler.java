package org.shsts.tinactory.content.logistics;

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
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperItemHandler implements IItemHandlerModifiable {
    protected final IItemHandlerModifiable compose;
    protected final List<Consumer<WrapperItemHandler>> listeners = new ArrayList<>();
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

    public void addListener(Consumer<WrapperItemHandler> cons) {
        this.listeners.add(cons);
    }

    public void onUpdate() {
        for (var cons : this.listeners) {
            cons.accept(this);
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.compose.setStackInSlot(slot, stack);
        this.onUpdate();
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
            this.onUpdate();
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
            this.onUpdate();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.compose.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.allowOutput && this.compose.isItemValid(slot, stack);
    }
}
