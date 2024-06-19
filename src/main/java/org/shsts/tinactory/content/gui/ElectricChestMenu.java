package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.MenuWidget;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.client.FluidSlot.HIGHLIGHT_COLOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChestMenu extends Menu<BlockEntity, ElectricChestMenu> {
    private final ElectricChest chest;
    private final Layout layout;

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
            return ItemHelper.copyWithCount(stack, count);
        }

        @Override
        public void set(ItemStack stack) {}

        @Override
        public ItemStack remove(int amount) {
            return ItemHelper.copyWithCount(chest.getStackInSlot(slot), amount);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            chest.extractItem(slot, stack.getCount());
        }
    }

    public ElectricChestMenu(SmartMenuType<?, ?> type, int id, Inventory inventory,
                             BlockEntity blockEntity, Layout layout) {
        super(type, id, inventory, blockEntity);
        this.chest = AllCapabilities.ELECTRIC_CHEST.get(blockEntity);
        this.layout = layout;

        var size = layout.slots.size() / 2;
        for (var i = 0; i < size; i++) {
            var syncSlot = addSyncSlot(itemSyncPacket(i));
            onSyncPacket(syncSlot, onItemSync(i));
        }
        for (var i = 0; i < size * 2; i++) {
            var slotInfo = layout.slots.get(i);
            var x = layout.getXOffset() + slotInfo.x() + MARGIN_HORIZONTAL + 1;
            var y = slotInfo.y() + MARGIN_TOP + 1;

            var slot = i < size ? new InputSlot(i, x, y) : new OutputSlot(i - size, x, y);
            addSlot(slot);
        }
        this.height = layout.rect.endY() + SLOT_SIZE + MARGIN_VERTICAL;
        onEventPacket(MenuEventHandler.CHEST_SLOT_CLICK, this::onClickSlot);
    }

    private SyncPacketFactory<BlockEntity, ChestItemSyncPacket> itemSyncPacket(int slot) {
        return (containerId, index, $) -> new ChestItemSyncPacket(containerId, index,
                chest.getStackInSlot(slot), chest.getFilter(slot).orElse(null));
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

    @OnlyIn(Dist.CLIENT)
    private class ItemSlot extends MenuWidget {
        private final int slot;

        public ItemSlot(int slot) {
            super(ElectricChestMenu.this);
            this.slot = slot;
        }

        private ItemStack getStack() {
            return chest.getStackInSlot(slot);
        }

        private Optional<ItemStack> getFilter() {
            return chest.getFilter(slot);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var stack = getStack();
            if (stack.isEmpty()) {
                getFilter().ifPresent(stack1 ->
                        RenderUtil.renderGhostItem(poseStack, stack1, rect.x(), rect.y()));
            } else {
                var s = String.valueOf(stack.getCount());
                RenderUtil.renderItemWithDecoration(stack, rect.x(), rect.y(), s);
            }

            if (isHovering(mouseX, mouseY)) {
                RenderSystem.colorMask(true, true, true, false);
                RenderUtil.fill(poseStack, rect, HIGHLIGHT_COLOR);
                RenderSystem.colorMask(true, true, true, true);
            }
        }

        @Override
        protected boolean canHover() {
            return true;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return Optional.of(getStack())
                    .filter(stack -> !stack.isEmpty())
                    .or(this::getFilter)
                    .map(ClientUtil::getTooltipsFromStack);
        }

        @Override
        protected boolean canClick(int button) {
            return true;
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            triggerEvent(MenuEventHandler.CHEST_SLOT_CLICK, (containerId, eventId) ->
                    new SlotEventPacket(containerId, eventId, slot, button));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public MenuScreen<ElectricChestMenu> createScreen(Inventory inventory, Component title) {
        var screen = super.createScreen(inventory, title);

        var layoutPanel = new Panel(screen);

        var size = layout.slots.size() / 2;
        for (var i = 0; i < size; i++) {
            var slot = layout.slots.get(i);
            var slot1 = layout.slots.get(i + size);
            var x = slot.x() + 1;
            var y = (slot.y() + slot1.y()) / 2 + 1;
            layoutPanel.addWidget(new Rect(x, y, SLOT_SIZE - 2, SLOT_SIZE - 2), new ItemSlot(i));
        }
        screen.addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
        return screen;
    }

    public static <T extends BlockEntity> Menu.Factory<T, ElectricChestMenu> factory(Layout layout) {
        return (type, id, inventory, be) -> new ElectricChestMenu(type, id, inventory, be, layout);
    }
}
