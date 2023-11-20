package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.gui.layout.AllLayouts;
import org.shsts.tinactory.gui.sync.CraftingSlot;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends ContainerMenu<SmartBlockEntity> {
    public WorkbenchMenu(ContainerMenuType<SmartBlockEntity, ?> type, int id,
                         Inventory inventory, SmartBlockEntity blockEntity) {
        super(type, id, inventory, blockEntity);
    }

    @Override
    public void initLayout() {
        super.initLayout();
        var workbench = this.blockEntity.getCapability(AllCapabilities.WORKBENCH.get())
                .orElseThrow(NoSuchElementException::new);

        var layout = AllLayouts.WORKBENCH;
        var slotInfo = layout.slots.get(0);
        var dx = (CONTENT_WIDTH - layout.rect.width()) / 2;

        this.addSlot((x, y) -> new CraftingSlot(workbench, x, y), dx + slotInfo.x(), slotInfo.y());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var oldStack = super.quickMoveStack(player, index);
        if (oldStack.isEmpty()) {
            return oldStack;
        }
        var slot = this.slots.get(index);
        var newStack = slot.getItem();
        return ItemHelper.itemStackEqual(oldStack, newStack) ? newStack : ItemStack.EMPTY;
    }
}
