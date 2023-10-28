package org.shsts.tinactory.gui;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;

public class CraftingSlot extends SlotItemHandler {
    protected WrapperItemHandler wrapperHandler;

    public CraftingSlot(WrapperItemHandler wrapperHandler, int index, int xPos, int yPos) {
        super(wrapperHandler, index, xPos, yPos);
        this.wrapperHandler = wrapperHandler;
    }

    @Override
    protected void onQuickCraft(ItemStack pStack, int pAmount) {
        super.onQuickCraft(pStack, pAmount);
    }
}
