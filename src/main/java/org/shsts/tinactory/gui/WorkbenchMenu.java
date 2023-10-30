package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.core.SmartBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends ContainerMenu<SmartBlockEntity> {
    public static final int OUTPUT_SLOT = 0;

    public WorkbenchMenu(ContainerMenuType<SmartBlockEntity, ?> type, int id,
                         Inventory inventory, SmartBlockEntity blockEntity) {
        super(type, id, inventory, blockEntity);
    }

    @Override
    public void initLayout() {
        this.addSlot(OUTPUT_SLOT, 6 * SLOT_SIZE, SLOT_SIZE);
        for (var j = 0; j < 9; j++) {
            this.addSlot(1 + j, j * SLOT_SIZE, 3 * SLOT_SIZE + SPACING_VERTICAL);
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                this.addSlot(10 + i * 3 + j, (2 + j) * SLOT_SIZE, i * SLOT_SIZE);
            }
        }
        this.height = 4 * SLOT_SIZE + SPACING_VERTICAL;
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
