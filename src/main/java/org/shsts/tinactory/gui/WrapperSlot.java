package org.shsts.tinactory.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WrapperSlot extends SlotItemHandler {
    protected final WrapperItemHandler wrapperHandler;
    protected final int index;

    public WrapperSlot(IItemHandler wrapperHandler, int index, int xPos, int yPos) {
        super(wrapperHandler, index, xPos, yPos);
        this.index = index;
        this.wrapperHandler = (WrapperItemHandler) wrapperHandler;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        this.wrapperHandler.invokeTake(this.index, player, stack);
    }
}
