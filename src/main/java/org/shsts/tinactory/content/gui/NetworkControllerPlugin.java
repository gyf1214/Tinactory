package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.StringUtils;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.IMenuPlugin;

import java.util.function.Consumer;

import static org.shsts.tinactory.content.AllMenus.RENAME;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.client.Widgets.EDIT_BOX_LINE_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerPlugin implements IMenuPlugin<NetworkControllerScreen> {
    public static final int BUTTON_SIZE = 24;
    public static final int PANEL_BORDER = 2;
    public static final int LEFT_WIDTH = PANEL_BORDER * 2 + BUTTON_SIZE * 5;
    public static final int RIGHT_WIDTH = LEFT_WIDTH + BUTTON_SIZE * 2;
    public static final int LEFT_OFFSET = LEFT_WIDTH + MARGIN_X * 2;
    public static final int WIDTH = LEFT_OFFSET + RIGHT_WIDTH;
    public static final int HEIGHT = BUTTON_SIZE * 6 + FONT_HEIGHT +
        MARGIN_VERTICAL * 2 + SPACING + PANEL_BORDER * 2;
    public static final int RENAME_INVENTORY_WIDTH = SLOT_SIZE * 9;
    public static final int RENAME_INVENTORY_MARGIN = (WIDTH - RENAME_INVENTORY_WIDTH) / 2;
    public static final int RENAME_INVENTORY_BAR_Y = HEIGHT - MARGIN_VERTICAL - SLOT_SIZE;
    public static final int RENAME_INVENTORY_Y = RENAME_INVENTORY_BAR_Y - SLOT_SIZE * 3 - SPACING;
    public static final int RENAME_BASE_WIDTH = RENAME_INVENTORY_WIDTH - SLOT_SIZE * 4;
    public static final int RENAME_BASE_MARGIN = RENAME_INVENTORY_MARGIN + SLOT_SIZE * 2;
    public static final int RENAME_BASE_Y = (RENAME_INVENTORY_Y - SLOT_SIZE -
        EDIT_BOX_LINE_HEIGHT - FONT_HEIGHT - MARGIN_VERTICAL * 2) / 2;
    public static final int RENAME_SLOT_Y = RENAME_BASE_Y + FONT_HEIGHT +
        EDIT_BOX_LINE_HEIGHT + MARGIN_VERTICAL * 2;

    private final IMenu menu;
    private String name = "";
    private boolean renameActive = false;
    private final WrapperItemHandler renameItem = new WrapperItemHandler(1);
    private ItemStack renameResult = ItemStack.EMPTY;
    private Consumer<String> onRefreshName = $ -> {};
    private boolean isTaking = false;

    private class RenameInventorySlot extends Slot {
        public RenameInventorySlot(int slot, int x, int y) {
            super(menu.inventory(), slot, x, y);
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
        public void set(ItemStack stack) {
            renameResult = stack;
        }

        @Override
        public ItemStack remove(int amount) {
            return renameResult.copy();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            isTaking = true;
            renameItem.setStackInSlot(0, ItemStack.EMPTY);
            refreshResult();
            isTaking = false;
        }

        @Override
        public boolean isActive() {
            return renameActive;
        }
    }

    public NetworkControllerPlugin(IMenu menu) {
        this.menu = menu;

        renameItem.setFilter(0, stack -> stack.is(Items.NAME_TAG));
        renameItem.onUpdate(this::refreshRenameItem);

        menu.setValidPredicate(() -> NetworkController
            .get(menu.blockEntity())
            .canPlayerInteract(menu.player()));
        menu.addSyncSlot("info", NetworkControllerSyncPacket::new);
        menu.onEventPacket(RENAME, p -> refreshName(p.getName(), false));
        menu.addSlot(new RenameInputSlot(MARGIN_X + RENAME_BASE_MARGIN + 1,
            MARGIN_TOP + RENAME_SLOT_Y + 1));
        menu.addSlot(new RenameResultSlot(MARGIN_X + RENAME_BASE_MARGIN + SLOT_SIZE * 4 + 1,
            MARGIN_TOP + RENAME_SLOT_Y + 1));

        for (var j = 0; j < 9; j++) {
            var x = MARGIN_X + RENAME_INVENTORY_MARGIN + j * SLOT_SIZE;
            var y = MARGIN_TOP + RENAME_INVENTORY_BAR_Y;
            menu.addSlot(new RenameInventorySlot(j, x + 1, y + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                var x = MARGIN_X + RENAME_INVENTORY_MARGIN + j * SLOT_SIZE;
                var y = MARGIN_TOP + RENAME_INVENTORY_Y + i * SLOT_SIZE;
                menu.addSlot(new RenameInventorySlot(9 + i * 9 + j, x + 1, y + 1));
            }
        }
    }

    public void setRenameActive(boolean val) {
        this.renameActive = val;
    }

    private void refreshResult() {
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

    private void refreshRenameItem() {
        if (isTaking) {
            return;
        }
        var item = renameItem.getStackInSlot(0);
        var name = item.hasCustomHoverName() ? item.getHoverName().getString() : "";
        refreshName(name, true);
    }

    private void refreshName(String name, boolean updateScreen) {
        this.name = name;
        if (updateScreen) {
            onRefreshName.accept(name);
        }
        refreshResult();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<NetworkControllerScreen> menuScreenClass() {
        return NetworkControllerScreen.class;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(NetworkControllerScreen screen) {
        onRefreshName = screen::refreshName;
    }

    @Override
    public void onMenuRemoved() {
        StackHelper.returnItemHandlerToPlayer(menu.player(), renameItem);
    }
}
