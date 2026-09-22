package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.gui.IFluidSlot;
import org.shsts.tinactory.api.gui.IItemSlot;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.SearchBox;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.STORAGE_SLOT;
import static org.shsts.tinactory.core.gui.Menu.SEARCH_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;
import static org.shsts.tinactory.integration.gui.client.SearchBox.SEARCH_ANCHOR;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StoragePanel extends ButtonPanel {
    public static final Rect BUTTON_OFFSET = PAGE_PANEL_OFFSET
        .offset(0, SEARCH_SIZE + SPACING).enlarge(0, -SEARCH_SIZE - SPACING);
    private static final Comparator<StorageEntry> DISPLAY_ORDER =
        Comparator.comparing(StorageEntry::key, StackHelper.KEY_DISPLAY_ORDER);

    private final List<StorageEntry> entries = new ArrayList<>();
    private final List<StorageEntry> displayEntries = new ArrayList<>();
    private final SearchBox searchBox;

    private class StorageButton extends ItemButton implements IFluidSlot, IItemSlot {
        public StorageButton(int slotIndex) {
            super(slotIndex);
        }

        @Override
        public FluidStack getFluidStack() {
            var index = itemIndex();
            if (index >= displayEntries.size()) {
                return FluidStack.EMPTY;
            }
            var entry = displayEntries.get(index);
            if (entry.key().type() != PortType.FLUID || entry.amount() <= 0L) {
                return FluidStack.EMPTY;
            }
            return StackHelper.FLUID_ADAPTER.stackOf(entry.key(), entry.amount());
        }

        @Override
        public ItemStack getItemStack() {
            var index = itemIndex();
            if (index >= displayEntries.size()) {
                return ItemStack.EMPTY;
            }
            var entry = displayEntries.get(index);
            if (entry.key().type() != PortType.ITEM || entry.amount() <= 0L) {
                return ItemStack.EMPTY;
            }
            return StackHelper.ITEM_ADAPTER.stackOf(entry.key());
        }
    }

    private final int maxSlots;

    public StoragePanel(MenuScreen<?> screen, int maxSlots) {
        super(screen, SLOT_SIZE, SLOT_SIZE, 0, BUTTON_OFFSET, true);
        this.maxSlots = maxSlots;
        this.searchBox = SearchBox.light(screen, this::refreshDisplayEntries);

        addChild(SEARCH_ANCHOR, Rect.ZERO, searchBox);
    }

    @Override
    protected ItemButton createSlot(int index) {
        return new StorageButton(index);
    }

    @Override
    protected int getItemCount() {
        var slotCount = gridViewGroup.getSlotCount();
        return Math.min(maxSlots, Math.max(1, (displayEntries.size() + slotCount) / slotCount) * slotCount);
    }

    @Override
    protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
        Rect rect, int index, boolean isHovering) {
        RenderUtil.blit(graphics, SLOT_BACKGROUND, rect);
        var rect1 = rect.offset(1, 1).enlarge(-2, -2);
        if (index < displayEntries.size()) {
            var entry = displayEntries.get(index);
            var display = entry.key().display(entry.amount());
            RenderUtil.renderDescriptorWithDecoration(graphics, display, rect1);
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
        var shiftPressed = ClientUtil.shiftDown();
        if (index < displayEntries.size()) {
            var entry = displayEntries.get(index);
            menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(entry.key(), entry.amount(), button,
                shiftPressed));
        } else {
            menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(button, shiftPressed));
        }
    }

    @Override
    protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
        if (index < displayEntries.size()) {
            var entry = displayEntries.get(index);
            return entry.key().tooltip(entry.amount());
        } else {
            return Optional.empty();
        }
    }

    private void refreshDisplayEntries(String query) {
        displayEntries.clear();
        entries.stream()
            .filter(entry -> StackHelper.matchText(query, entry.key()))
            .forEach(displayEntries::add);
        refresh();
    }

    public void sync(StorageSyncPacket packet) {
        entries.clear();
        entries.addAll(packet.entries());
        entries.sort(DISPLAY_ORDER);
        refreshDisplayEntries(searchBox.getValue());
    }
}
