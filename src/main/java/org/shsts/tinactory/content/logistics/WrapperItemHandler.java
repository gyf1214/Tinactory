package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperItemHandler implements IItemHandlerModifiable {
    protected final IItemHandlerModifiable compose;

    protected WrapperItemHandler(IItemHandlerModifiable compose) {
        this.compose = compose;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
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
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.compose.insertItem(slot, stack, simulate);
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
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.compose.isItemValid(slot, stack);
    }
}
