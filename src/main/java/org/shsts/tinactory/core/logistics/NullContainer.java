package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface NullContainer extends Container {
    @Override
    default int getContainerSize() {
        return 0;
    }

    @Override
    default boolean isEmpty() {
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setItem(int slot, ItemStack stack) {}

    @Override
    default void setChanged() {}

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default void clearContent() {}
}
