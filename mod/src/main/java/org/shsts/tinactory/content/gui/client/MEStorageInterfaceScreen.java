package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.gui.MEStorageInterfaceMenu;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.logistics.FluidStackWrapper;
import org.shsts.tinactory.core.logistics.ItemStackWrapper;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.ME_STORAGE_INTERFACE_SLOT;
import static org.shsts.tinactory.content.gui.MEStorageInterfaceMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.gui.MEStorageInterfaceMenu.SLOT_SYNC;
import static org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket.QUICK_MOVE_BUTTON;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceScreen extends MenuScreen<MEStorageInterfaceMenu> {
    private static final int SLOT_COUNT = 6 * 9;

    private final List<ItemStack> items = new ArrayList<>();
    private final List<FluidStack> fluids = new ArrayList<>();

    private class StoragePanel extends ButtonPanel {
        public StoragePanel() {
            super(MEStorageInterfaceScreen.this, SLOT_SIZE, SLOT_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            var size = items.size() + fluids.size();
            return Math.max(1, (size + SLOT_COUNT) / SLOT_COUNT) * SLOT_COUNT;
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            RenderUtil.blit(poseStack, SLOT_BACKGROUND, getBlitOffset(), rect);
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            if (index < items.size()) {
                var item = items.get(index);
                RenderUtil.renderItemWithDecoration(item, rect1.x(), rect1.y());
            } else if (index - items.size() < fluids.size()) {
                var fluid = fluids.get(index - items.size());
                RenderUtil.renderFluidWithDecoration(poseStack, fluid, rect1, getBlitOffset());
            }
            if (isHovering) {
                RenderUtil.renderSlotHover(poseStack, rect1);
            }
        }

        @Override
        protected boolean canClickButton(int index, double mouseX, double mouseY, int button) {
            return button == 0 || button == 1;
        }

        @Override
        protected void playButtonSound() {}

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (index < items.size()) {
                var item = items.get(index);
                var button1 = ClientUtil.shiftDown() ? QUICK_MOVE_BUTTON : button;
                menu.triggerEvent(ME_STORAGE_INTERFACE_SLOT,
                    () -> new MEStorageInterfaceEventPacket(item, button1));
            } else if (index - items.size() < fluids.size()) {
                ClientUtil.playSound(SoundEvents.BUCKET_FILL);
                var fluid = fluids.get(index - items.size());
                menu.triggerEvent(ME_STORAGE_INTERFACE_SLOT,
                    () -> new MEStorageInterfaceEventPacket(fluid, button));
            } else {
                menu.triggerEvent(ME_STORAGE_INTERFACE_SLOT,
                    () -> new MEStorageInterfaceEventPacket(button));
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            if (index < items.size()) {
                var item = items.get(index);
                return Optional.of(ClientUtil.itemTooltip(item));
            } else if (index - items.size() < fluids.size()) {
                var fluid = fluids.get(index - items.size());
                return Optional.of(ClientUtil.fluidTooltip(fluid, true));
            } else {
                return Optional.empty();
            }
        }
    }

    private final StoragePanel panel;

    public MEStorageInterfaceScreen(MEStorageInterfaceMenu menu, Component title) {
        super(menu, title);
        this.contentHeight = menu.endY();

        this.panel = new StoragePanel();
        addPanel(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, PANEL_HEIGHT), panel);
        menu.onSyncPacket(SLOT_SYNC, this::onSync);
    }

    private void onSync(MEStorageInterfaceSyncPacket packet) {
        var itemsMap = new HashMap<ItemStackWrapper, ItemStack>();
        for (var newItem : packet.items()) {
            var key = new ItemStackWrapper(newItem);
            if (itemsMap.containsKey(key)) {
                itemsMap.get(key).grow(newItem.getCount());
            } else {
                itemsMap.put(key, newItem);
            }
        }
        items.clear();
        items.addAll(itemsMap.values());
        items.sort(Comparator.comparing($ -> $.getItem().getRegistryName()));

        var fluidsMap = new HashMap<FluidStackWrapper, FluidStack>();
        for (var newFluid : packet.fluids()) {
            var key = new FluidStackWrapper(newFluid);
            if (fluidsMap.containsKey(key)) {
                fluidsMap.get(key).grow(newFluid.getAmount());
            } else {
                fluidsMap.put(key, newFluid);
            }
        }
        fluids.clear();
        fluids.addAll(fluidsMap.values());
        fluids.sort(Comparator.comparing($ -> $.getFluid().getRegistryName()));

        panel.refresh();
    }
}
