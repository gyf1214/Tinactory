package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.gui.MEStorageInterfaceMenu;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.STORAGE_SLOT;
import static org.shsts.tinactory.content.gui.MEStorageInterfaceMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.gui.MEStorageInterfaceMenu.SLOT_SYNC;
import static org.shsts.tinactory.content.gui.sync.StorageEventPacket.QUICK_MOVE_BUTTON;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceScreen extends MenuScreen<MEStorageInterfaceMenu> {
    private final List<ItemStack> items = new ArrayList<>();
    private final List<FluidStack> fluids = new ArrayList<>();

    private class StoragePanel extends ButtonPanel {
        public StoragePanel() {
            super(MEStorageInterfaceScreen.this, SLOT_SIZE, SLOT_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            var count = items.size() + fluids.size();
            var slotCount = gridViewGroup.getSlotCount();
            return Math.max(1, (count + slotCount) / slotCount) * slotCount;
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            RenderUtil.blit(graphics, SLOT_BACKGROUND, rect);
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            if (index < items.size()) {
                var item = items.get(index);
                RenderUtil.renderItemWithDecoration(graphics, item, rect1.x(), rect1.y());
            } else if (index - items.size() < fluids.size()) {
                var fluid = fluids.get(index - items.size());
                RenderUtil.renderFluidWithDecoration(graphics, fluid, rect1);
            }
            if (isHovering) {
                RenderUtil.renderSlotHover(graphics, rect1);
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
                menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(item, button1));
            } else if (index - items.size() < fluids.size()) {
                ClientUtil.playSound(SoundEvents.BUCKET_FILL);
                var fluid = fluids.get(index - items.size());
                menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(fluid, button));
            } else {
                menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(button));
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
        rootPanel.addChild(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, PANEL_HEIGHT), panel);
        menu.onSyncPacket(SLOT_SYNC, this::onSync);
    }

    private void onSync(StorageSyncPacket packet) {
        var itemsMap = new HashMap<IStackKey, ItemStack>();
        var fluidsMap = new HashMap<IStackKey, FluidStack>();

        for (var entry : packet.entries()) {
            if (entry.isFilter()) {
                continue;
            }
            var key = entry.key();
            if (key.type() == PortType.ITEM) {
                if (itemsMap.containsKey(key)) {
                    itemsMap.get(key).grow(entry.amount());
                } else {
                    itemsMap.put(key, StackHelper.ITEM_ADAPTER.stackOf(key, entry.amount()));
                }
            } else if (key.type() == PortType.FLUID) {
                if (fluidsMap.containsKey(key)) {
                    fluidsMap.get(key).grow(entry.amount());
                } else {
                    fluidsMap.put(key, StackHelper.FLUID_ADAPTER.stackOf(key, entry.amount()));
                }
            }
        }

        items.clear();
        items.addAll(itemsMap.values());
        items.sort(Comparator.comparing($ -> ClientUtil.getRegistryKey(Registries.ITEM, $.getItem()),
            ResourceLocation::compareNamespaced));
        fluids.clear();
        fluids.addAll(fluidsMap.values());
        fluids.sort(Comparator.comparing($ -> ClientUtil.getRegistryKey(Registries.FLUID, $.getFluid()),
            ResourceLocation::compareNamespaced));

        panel.refresh();
    }
}
