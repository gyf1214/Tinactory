package org.shsts.tinactory.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ContainerMenu<T extends BlockEntity> extends AbstractContainerMenu {
    public static final int WIDTH = 176;
    public static final int SLOT_SIZE = 18;
    public static final int MARGIN_HORIZONTAL = (WIDTH - 9 * SLOT_SIZE) / 2;
    public static final int SPACING_VERTICAL = 3;
    public static final int MARGIN_VERTICAL = 3 + SPACING_VERTICAL;
    public static final int FONT_HEIGHT = 9;

    protected final Player player;
    protected final Inventory inventory;
    protected final T blockEntity;
    protected int height;

    public ContainerMenu(ContainerMenuType<T, ?> type, int id, Inventory inventory, T blockEntity) {
        super(type, id);
        this.player = inventory.player;
        this.inventory = inventory;
        this.blockEntity = blockEntity;
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
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                this.addSlot(new Slot(this.inventory, 9 + i * 9 + j, MARGIN_HORIZONTAL + j * SLOT_SIZE, y));
            }
            y += SLOT_SIZE;
        }
        y += SPACING_VERTICAL;
        for (var j = 0; j < 9; j++) {
            this.addSlot(new Slot(this.inventory, j, MARGIN_HORIZONTAL + j * SLOT_SIZE, y));
        }
        return y + SLOT_SIZE + MARGIN_VERTICAL;
    }

    public void setLayout(List<Rect> widgets, boolean hasInventory) {
        var y = 0;
        for (var widget : widgets) {
            y = Math.max(y, widget.endY());
        }
        y += MARGIN_VERTICAL + FONT_HEIGHT + SPACING_VERTICAL;
        if (hasInventory) {
            y = addInventorySlots(y);
        } else {
            y += MARGIN_VERTICAL;
        }
        this.height = y;
    }

    public int getHeight() {
        return height;
    }

    protected void addSlot(int slotIndex, int posX, int posY) {
        var itemHandler = this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(NullPointerException::new);
        this.addSlot(new SlotItemHandler(itemHandler, slotIndex, posX, posY));
    }

    public interface Factory<T1 extends BlockEntity, M1 extends ContainerMenu<T1>> {
        M1 create(ContainerMenuType<T1, M1> type, int id, Inventory inventory, T1 blockEntity);
    }
}
