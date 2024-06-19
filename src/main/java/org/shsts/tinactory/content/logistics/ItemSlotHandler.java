package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemSlotHandler extends ItemStackHandler {
    private final int limit;

    public ItemSlotHandler(int size, int limit) {
        super(size);
        this.limit = limit;
    }

    @Override
    public int getSlotLimit(int slot) {
        return limit;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return limit;
    }
}
