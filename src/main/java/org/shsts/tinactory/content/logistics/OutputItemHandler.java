package org.shsts.tinactory.content.logistics;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class OutputItemHandler implements IItemHandlerModifiable {
    private final IItemHandlerModifiable compose;

    public OutputItemHandler(IItemHandlerModifiable compose) {
        this.compose = compose;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.compose.setStackInSlot(slot, stack);
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
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.compose.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.compose.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }
}
