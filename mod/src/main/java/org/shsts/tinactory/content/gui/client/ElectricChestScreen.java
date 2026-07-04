package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.ElectricChestMenu;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.integration.gui.client.MenuWidget;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.ITEM_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChestScreen extends ElectricStorageScreen<ElectricChestMenu> {
    private final ElectricChest chest;

    private class ItemSlot extends MenuWidget {
        private final int slot;

        public ItemSlot(int slot) {
            super(menu());
            this.slot = slot;
        }

        private ItemStack getStack() {
            return chest.getStackInSlot(slot);
        }

        private Optional<ItemStack> getFilter() {
            return chest.getFilter(slot);
        }

        @Override
        public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var stack = getStack();
            if (stack.isEmpty()) {
                getFilter().ifPresent(stack1 ->
                    RenderUtil.renderGhostItem(graphics, stack1, rect.x(), rect.y()));
            } else {
                RenderUtil.renderItemWithDecoration(graphics, stack, rect.x(), rect.y());
            }

            if (isHovered(mouseX, mouseY)) {
                RenderUtil.renderSlotHover(graphics, rect);
            }
        }

        @Override
        public boolean canHover() {
            return true;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return Optional.of(getStack())
                .filter(stack -> !stack.isEmpty())
                .or(this::getFilter)
                .map(ClientUtil::itemTooltip);
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return true;
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            menu.triggerEvent(ITEM_SLOT_CLICK, () -> new SlotEventPacket(slot, button));
        }
    }

    public ElectricChestScreen(ElectricChestMenu menu, Component title) {
        super(menu, title);

        this.chest = menu.chest();
        var size = layout.slots.size() / 2;
        for (var i = 0; i < size; i++) {
            var slot = layout.slots.get(i);
            var slot1 = layout.slots.get(i + size);
            var x = slot.x() + 1;
            var y = (slot.y() + slot1.y()) / 2 + 1;
            layoutPanel.addChild(new Rect(x, y, SLOT_SIZE - 2, SLOT_SIZE - 2), new ItemSlot(i));
        }
    }
}
