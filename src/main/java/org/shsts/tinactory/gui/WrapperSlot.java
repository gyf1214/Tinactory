package org.shsts.tinactory.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WrapperSlot extends SlotItemHandler {
    protected final @Nullable WrapperItemHandler wrapperHandler;
    protected final int index;

    public WrapperSlot(IItemHandler handler, int index, int xPos, int yPos) {
        super(handler, index, xPos, yPos);
        this.index = index;
        if (handler instanceof WrapperItemHandler wrapperHandler1) {
            this.wrapperHandler = wrapperHandler1;
        } else {
            this.wrapperHandler = null;
        }
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        if (this.wrapperHandler != null) {
            this.wrapperHandler.invokeTake(this.index, player, stack);
        }
    }
}
