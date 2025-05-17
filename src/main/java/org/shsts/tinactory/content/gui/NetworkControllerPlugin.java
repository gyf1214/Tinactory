package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.StringUtils;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.IMenuPlugin;

import static org.shsts.tinactory.content.AllMenus.RENAME;
import static org.shsts.tinactory.content.gui.client.TechPanel.PANEL_BORDER;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerPlugin implements IMenuPlugin<NetworkControllerScreen> {
    public static final int WIDTH = TechPanel.LEFT_OFFSET + TechPanel.RIGHT_WIDTH;
    public static final int HEIGHT = TechPanel.BUTTON_SIZE * 6 + FONT_HEIGHT +
        MARGIN_VERTICAL * 2 + SPACING + PANEL_BORDER * 2;
    public static final int RENAME_SLOT_MARGIN = (WIDTH - SLOT_SIZE * 9) / 2;
    public static final int RENAME_INVENTORY_BAR_Y = HEIGHT - MARGIN_VERTICAL - SLOT_SIZE;

    private final IMenu menu;
    private String name = "";
    private boolean renameActive = false;
    private final WrapperItemHandler renameItem = new WrapperItemHandler(1);
    private ItemStack renameResult = ItemStack.EMPTY;

    private class RenameInventorySlot extends Slot {
        public RenameInventorySlot(int pSlot, int pX, int pY) {
            super(menu.inventory(), pSlot, pX, pY);
        }

        @Override
        public boolean isActive() {
            return renameActive;
        }
    }

    private class RenameInputSlot extends SlotItemHandler {
        public RenameInputSlot(int x, int y) {
            super(renameItem, 0, x, y);
        }

        @Override
        public boolean isActive() {
            return renameActive;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    private class RenameResultSlot extends Slot {
        public RenameResultSlot(int x, int y) {
            super(Menu.EMPTY_CONTAINER, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !renameResult.isEmpty();
        }

        @Override
        public ItemStack getItem() {
            return renameResult;
        }

        @Override
        public void set(ItemStack stack) {}

        @Override
        public ItemStack remove(int amount) {
            return renameResult.copy();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            renameItem.getStackInSlot(0).shrink(1);
            refreshRename();
        }

        @Override
        public boolean isActive() {
            return renameActive;
        }
    }

    public NetworkControllerPlugin(IMenu menu) {
        this.menu = menu;

        renameItem.setFilter(0, stack -> stack.is(Items.NAME_TAG));
        renameItem.onUpdate(this::refreshRename);

        menu.setValidPredicate(() -> NetworkController
            .get(menu.blockEntity())
            .canPlayerInteract(menu.player()));
        menu.addSyncSlot("info", NetworkControllerSyncPacket::new);
        menu.onEventPacket(RENAME, p -> refreshRename(p.getName()));
        menu.addSlot(new RenameInputSlot(100, 100));
        menu.addSlot(new RenameResultSlot(200, 100));

        var y = 120;
        var barY = y + 3 * SLOT_SIZE + SPACING;
        for (var j = 0; j < 9; j++) {
            var x = MARGIN_X + j * SLOT_SIZE;
            menu.addSlot(new RenameInventorySlot(j, x + 1, barY + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                var x = MARGIN_X + j * SLOT_SIZE;
                var y1 = y + i * SLOT_SIZE + MARGIN_TOP;
                menu.addSlot(new RenameInventorySlot(9 + i * 9 + j, x + 1, y1 + 1));
            }
        }
    }

    public void setRenameActive(boolean val) {
        this.renameActive = val;
    }

    private void refreshRename() {
        var item = renameItem.getStackInSlot(0);
        if (item.is(Items.NAME_TAG)) {
            renameResult = item.copy();
            if (StringUtils.isBlank(name)) {
                renameResult.resetHoverName();
            } else {
                renameResult.setHoverName(new TextComponent(name));
            }
        } else {
            renameResult = ItemStack.EMPTY;
        }
    }

    private void refreshRename(String name) {
        this.name = name;
        refreshRename();
    }

    @Override
    public void onMenuRemoved() {
        StackHelper.returnItemHandlerToPlayer(menu.player(), renameItem);
    }
}
