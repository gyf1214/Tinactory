package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.StorageMenu;
import org.shsts.tinactory.content.gui.sync.FilterEventPacket;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.StretchImage;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.FILTER_SLOT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FilterPanel extends Panel {
    private static final Texture BG_TEX = new Texture(gregtech("gui/tab/tabs_left"), 64, 84);
    private static final Rect BG_TEX_RECT = new Rect(32, 0, 32, 28);
    private static final int PANEL_BORDER = 4;
    private static final int SLOTS_PER_LINE = 3;
    private static final int PANEL_WIDTH = SLOT_SIZE * SLOTS_PER_LINE + MARGIN_X * 2 + PANEL_BORDER;
    private static final Rect SLOTS_OFFSET = Rect.corners(MARGIN_X, MARGIN_VERTICAL,
        -MARGIN_X - PANEL_BORDER, -MARGIN_VERTICAL);

    private final StorageMenu storageMenu;
    private final int filterSlots;

    private class SlotPanel extends ButtonPanel {
        private final FilterEntry.ClickHelper clickHelper = new FilterEntry.ClickHelper();

        public SlotPanel() {
            super(FilterPanel.this.screen, SLOT_SIZE, SLOT_SIZE, 0, false);
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            Rect rect, int index, boolean isHovering) {
            RenderUtil.blit(graphics, SLOT_BACKGROUND, rect);
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            RenderUtil.renderDescriptor(graphics, storageMenu.getFilter(index).display(), rect1);
            if (isHovering) {
                RenderUtil.renderSlotHover(graphics, rect1);
            }
        }

        @Override
        protected boolean canClickButton(int index, double mouseX, double mouseY, int button) {
            return button == 0 || button == 1;
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            var oldFilter = storageMenu.getFilter(index);
            var newFilter = oldFilter.click(index, clickHelper, button, menu.getCarried(),
                storageMenu.allowItemFilter(),
                storageMenu.allowFluidFilter(),
                storageMenu.allowTagFilter());

            if (newFilter.isPresent()) {
                var event = new FilterEventPacket(oldFilter.type() != FilterEntry.Type.NONE,
                    index, newFilter.get());
                menu.triggerEvent(FILTER_SLOT, () -> event);
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return storageMenu.getFilter(index).tooltip();
        }

        @Override
        protected int getItemCount() {
            return filterSlots;
        }
    }

    public FilterPanel(MenuScreen<? extends StorageMenu> screen, int filterSlots) {
        super(screen);
        this.storageMenu = screen.menu();
        this.filterSlots = filterSlots;

        var bg = new StretchImage(storageMenu, BG_TEX, BG_TEX_RECT, PANEL_BORDER);
        addChild(RectD.FULL, Rect.ZERO, bg);

        var slotPanel = new SlotPanel();
        addGroup(SLOTS_OFFSET, slotPanel);
    }

    public static Rect panelOffset(int filterSlots) {
        var lines = (filterSlots + SLOTS_PER_LINE - 1) / SLOTS_PER_LINE;
        return new Rect(-MARGIN_X - PANEL_WIDTH + PANEL_BORDER,
            -MARGIN_VERTICAL, PANEL_WIDTH, SLOT_SIZE * lines + MARGIN_VERTICAL * 2);
    }
}
