package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NotifyItemHandler extends WrapperItemHandler {
    private final Runnable callback;

    public NotifyItemHandler(IItemHandlerModifiable compose, Runnable callback) {
        super(compose);
        this.callback = callback;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        super.setStackInSlot(slot, stack);
        this.callback.run();
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        var ret = super.insertItem(slot, stack, simulate);
        if (!simulate && ret.getCount() != stack.getCount()) {
            this.callback.run();
        }
        return ret;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var ret = super.extractItem(slot, amount, simulate);
        if (!simulate && !ret.isEmpty()) {
            this.callback.run();
        }
        return ret;
    }
}
