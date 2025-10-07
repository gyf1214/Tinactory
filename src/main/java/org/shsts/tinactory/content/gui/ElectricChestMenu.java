package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.logistics.StackHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllMenus.ITEM_SLOT_CLICK;
import static org.shsts.tinactory.core.common.CapabilityProvider.getProvider;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChestMenu extends ElectricStorageMenu {
    private final ElectricChest chest;

    private class InputSlot extends Slot {
        private final int slot;

        public InputSlot(int slot, int x, int y) {
            super(Menu.EMPTY_CONTAINER, slot, x, y);
            this.slot = slot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return chest.allowStackInSlot(slot, stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            var capacity = chest.capacity - chest.getStackInSlot(slot).getCount();
            return Math.min(capacity, 64);
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            chest.insertItem(slot, stack);
        }
    }

    private class OutputSlot extends Slot {
        private final int slot;

        public OutputSlot(int slot, int x, int y) {
            super(Menu.EMPTY_CONTAINER, slot, x, y);
            this.slot = slot;
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
        public ItemStack getItem() {
            var stack = chest.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            var count = Math.min(stack.getCount(), stack.getMaxStackSize());
            return StackHelper.copyWithCount(stack, count);
        }

        @Override
        public void set(ItemStack stack) {}

        @Override
        public ItemStack remove(int amount) {
            return StackHelper.copyWithCount(chest.getStackInSlot(slot), amount);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            chest.extractItem(slot, stack.getCount());
        }
    }

    public ElectricChestMenu(Properties properties) {
        super(properties);
        this.chest = getProvider(blockEntity, ElectricChest.ID, ElectricChest.class);

        var size = layout.slots.size() / 2;
        for (var i = 0; i < size; i++) {
            var syncSlot = addSyncSlot(itemSyncPacket(i));
            onSyncPacket(syncSlot, onItemSync(i));
        }
        for (var i = 0; i < size * 2; i++) {
            var slotInfo = layout.slots.get(i);
            var x = layout.getXOffset() + slotInfo.x() + MARGIN_X + 1;
            var y = slotInfo.y() + MARGIN_TOP + 1;

            var slot = i < size ? new InputSlot(i, x, y) : new OutputSlot(i - size, x, y);
            addSlot(slot);
        }
        onEventPacket(ITEM_SLOT_CLICK, this::onClickSlot);
    }

    public ElectricChest chest() {
        return chest;
    }

    private Supplier<ChestItemSyncPacket> itemSyncPacket(int slot) {
        return () -> new ChestItemSyncPacket(chest.getStackInSlot(slot),
            chest.getFilter(slot).orElse(null));
    }

    private Consumer<ChestItemSyncPacket> onItemSync(int slot) {
        return p -> {
            chest.setStackInSlot(slot, p.getStack());
            p.getFilter().ifPresentOrElse(stack -> chest.setFilter(slot, stack),
                () -> chest.resetFilter(slot));
        };
    }

    private void onClickSlot(SlotEventPacket p) {
        var carried = getCarried();
        var slot = p.getIndex();
        if (!chest.getStackInSlot(slot).isEmpty()) {
            return;
        }
        if (carried.isEmpty()) {
            chest.resetFilter(slot);
        } else {
            chest.setFilter(slot, carried);
        }
    }
}
