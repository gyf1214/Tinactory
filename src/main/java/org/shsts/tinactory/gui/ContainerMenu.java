package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerMenu<T extends BlockEntity> extends AbstractContainerMenu {
    public static final int WIDTH = 176;
    public static final int SLOT_SIZE = 18;
    public static final int CONTENT_WIDTH = 9 * SLOT_SIZE;
    public static final int MARGIN_HORIZONTAL = (WIDTH - CONTENT_WIDTH) / 2;
    public static final int FONT_HEIGHT = 9;
    public static final int SPACING_VERTICAL = 3;
    public static final int MARGIN_VERTICAL = 3 + SPACING_VERTICAL;
    public static final int MARGIN_TOP = MARGIN_VERTICAL + FONT_HEIGHT + SPACING_VERTICAL;

    public final boolean isClientSide;
    public final T blockEntity;
    public final Player player;
    public final Inventory inventory;

    protected final LazyOptional<IItemHandler> container;
    protected int containerSlotCount;
    protected boolean hasInventory;
    protected int height;
    protected final List<ContainerSyncData<?, ContainerMenu<T>>> syncData = new ArrayList<>();

    public ContainerMenu(ContainerMenuType<T, ?> type, int id, Inventory inventory, T blockEntity) {
        super(type, id);
        this.player = inventory.player;
        this.inventory = inventory;
        this.blockEntity = blockEntity;
        assert blockEntity.getLevel() != null;
        this.isClientSide = blockEntity.getLevel().isClientSide;
        this.container = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        this.height = 0;
    }

    @Override
    public boolean stillValid(Player player) {
        var be = this.blockEntity;
        var level = be.getLevel();
        var pos = be.getBlockPos();
        return player == this.player &&
                level == player.getLevel() &&
                level.getBlockEntity(pos) == be &&
                player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0;
    }

    protected int addInventorySlots(int y) {
        var barY = y + 3 * SLOT_SIZE + SPACING_VERTICAL;
        for (var j = 0; j < 9; j++) {
            this.addSlot(new Slot(this.inventory, j, MARGIN_HORIZONTAL + j * SLOT_SIZE + 1, barY + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                this.addSlot(new Slot(this.inventory, 9 + i * 9 + j,
                        MARGIN_HORIZONTAL + j * SLOT_SIZE + 1, y + i * SLOT_SIZE + 1));
            }
        }
        return barY + SLOT_SIZE + MARGIN_VERTICAL;
    }

    /**
     * This is called before any menu callbacks
     */
    public void initLayout() {}

    /**
     * This is called after all menu callbacks
     */
    public void setLayout(List<Rect> widgets, boolean hasInventory) {
        // here height does not include top margin and spacing
        var y = this.height;
        for (var widget : widgets) {
            y = Math.max(y, widget.endY());
        }
        y += MARGIN_TOP + SPACING_VERTICAL;
        this.containerSlotCount = this.slots.size();
        this.hasInventory = hasInventory;
        if (hasInventory) {
            y = addInventorySlots(y);
        } else {
            y += MARGIN_VERTICAL;
        }
        this.height = y;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!this.hasInventory || index < 0 || index >= this.slots.size() || this.isClientSide) {
            return ItemStack.EMPTY;
        }
        var slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        var inv = new PlayerMainInvWrapper(this.inventory);
        if (index < this.containerSlotCount) {
            if (!slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            var oldStack = slot.getItem().copy();
            var stack = oldStack.copy();
            var reminder = ItemHandlerHelper.insertItemStacked(inv, stack, true);
            stack.shrink(reminder.getCount());
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            var stack1 = slot.safeTake(stack.getCount(), Integer.MAX_VALUE, player);
            ItemHandlerHelper.insertItemStacked(inv, stack1, false);

            return oldStack;
        } else {
            var invIndex = index - this.containerSlotCount;
            var oldStack = inv.getStackInSlot(invIndex).copy();
            var reminder = oldStack.copy();
            var amount = reminder.getCount();
            for (var i = 0; i < this.containerSlotCount; i++) {
                if (reminder.isEmpty()) {
                    break;
                }
                var targetSlot = this.slots.get(i);
                reminder = targetSlot.safeInsert(reminder);
            }
            inv.extractItem(invIndex, amount - reminder.getCount(), false);

            return oldStack;
        }
    }

    public int getHeight() {
        return height;
    }

    @FunctionalInterface
    public interface SlotFactory<T extends Slot> {
        T create(IItemHandler itemHandler, int index, int posX, int posY);
    }

    public void addSlot(int slotIndex, int posX, int posY) {
        this.addSlot(WrapperSlot::new, slotIndex, posX, posY);
    }

    public void addSlot(SlotFactory<?> factory, int slotIndex, int posX, int posY) {
        var itemHandler = this.container.orElseThrow(NoSuchElementException::new);
        this.addSlot(factory.create(itemHandler, slotIndex,
                posX + MARGIN_HORIZONTAL + 1, posY + MARGIN_TOP + 1));
    }

    public <U> int addSyncData(ContainerSyncData<U, ContainerMenu<T>> syncData) {
        this.syncData.add(syncData);
        this.addDataSlots(syncData);
        return this.syncData.size() - 1;
    }

    public <U> U getSyncData(int index, Class<U> clazz) {
        return clazz.cast(this.syncData.get(index).value);
    }

    public short getSimpleData(int index) {
        return this.getSyncData(index, Short.class);
    }

    public interface Factory<T1 extends BlockEntity, M1 extends ContainerMenu<T1>> {
        M1 create(ContainerMenuType<T1, M1> type, int id, Inventory inventory, T1 blockEntity);
    }
}
