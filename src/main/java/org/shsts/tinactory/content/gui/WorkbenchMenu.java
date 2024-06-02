package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.sync.CraftingSlot;
import org.shsts.tinactory.core.logistics.ItemHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends Menu<SmartBlockEntity, WorkbenchMenu> {
    public WorkbenchMenu(SmartMenuType<SmartBlockEntity, ?> type, int id,
                         Inventory inventory, SmartBlockEntity blockEntity) {
        super(type, id, inventory, blockEntity);

        var layout = AllLayouts.WORKBENCH;
        var workbench = AllCapabilities.WORKBENCH.get(blockEntity);
        var xOffset = layout.getXOffset();
        var outputX = 0;
        var outputY = 0;
        for (var slot : layout.slots) {
            var x = xOffset + slot.x();
            var y = slot.y();
            if (slot.type() == SlotType.NONE) {
                outputX = x;
                outputY = y;
            } else if (slot.type() == SlotType.ITEM_INPUT) {
                addSlot(slot.index(), x, y);
            } else {
                throw new IllegalArgumentException();
            }
        }
        addSlot(new CraftingSlot(workbench, outputX + MARGIN_HORIZONTAL + 1, outputY + MARGIN_TOP + 1));
        this.height = layout.rect.endY();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var oldStack = super.quickMoveStack(player, index);
        if (oldStack.isEmpty()) {
            return oldStack;
        }
        var slot = slots.get(index);
        var newStack = slot.getItem();
        return ItemHelper.itemStackEqual(oldStack, newStack) ? newStack : ItemStack.EMPTY;
    }
}
