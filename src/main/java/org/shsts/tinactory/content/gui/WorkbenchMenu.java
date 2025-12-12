package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.AllLayouts;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.logistics.StackHelper;

import static org.shsts.tinactory.core.gui.Menu.EMPTY_CONTAINER;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends LayoutMenu {
    private class ResultSlot extends Slot {
        public ResultSlot(int x, int y) {
            super(EMPTY_CONTAINER, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !getItem().isEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public ItemStack getItem() {
            return workbench.getResult();
        }

        @Override
        public void set(ItemStack stack) {
            workbench.setResult(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return getItem().copy();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            workbench.onTake(player, stack);
        }
    }

    private final Workbench workbench;

    public WorkbenchMenu(Properties properties) {
        super(properties, AllLayouts.WORKBENCH, 0);
        addLayoutSlots(layout);

        this.workbench = Workbench.get(blockEntity());
        for (var slot : layout.slots) {
            if (slot.type() == SlotType.NONE) {
                var x = slot.x() + layout.getXOffset() + MARGIN_X + 1;
                var y = slot.y() + MARGIN_TOP + 1;
                addSlot(new ResultSlot(x, y));
            }
        }
    }

    @Override
    protected boolean quickMoveStack(Slot slot) {
        var oldStack = slot.getItem().copy();
        if (!super.quickMoveStack(slot)) {
            return false;
        }
        return StackHelper.itemStackEqual(oldStack, slot.getItem());
    }
}
