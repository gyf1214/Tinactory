package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.StorageMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Widgets;

import static org.shsts.tinactory.content.gui.StorageMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.gui.StorageMenu.SLOT_SYNC;
import static org.shsts.tinactory.core.gui.Menu.SEARCH_TOP_MARGIN;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageScreen extends MenuScreen<StorageMenu> {
    @Nullable
    private final FilterPanel filterPanel;

    public StorageScreen(StorageMenu menu, Component title) {
        super(menu, title);
        this.contentHeight = menu.endY();

        var storagePanel = new StoragePanel(this, menu.storageSlots());
        rootPanel.addChild(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, PANEL_HEIGHT), storagePanel);
        menu.onSyncPacket(SLOT_SYNC, storagePanel::sync);

        var filterSlots = menu.filterSlots();
        if (filterSlots > 0) {
            this.filterPanel = new FilterPanel(this, filterSlots);
            var offset = FilterPanel.panelOffset(filterSlots).offset(0, SEARCH_TOP_MARGIN);
            rootPanel.addChild(offset, filterPanel);
            filterPanel.setActive(false);

            Widgets.gregtechButton(menu, rootPanel, filterPanel, RectD.ZERO, 0, PANEL_HEIGHT - SLOT_SIZE,
                I18n.tr("tinactory.tooltip.openFilterPanel"), () -> {});
        } else {
            filterPanel = null;
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
        if (!super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, button)) {
            return false;
        }
        return filterPanel == null || !filterPanel.isActive() || !filterPanel.mouseIn(mouseX, mouseY);
    }
}
