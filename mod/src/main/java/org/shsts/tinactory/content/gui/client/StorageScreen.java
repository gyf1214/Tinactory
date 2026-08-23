package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.InventoryMenu;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

import static org.shsts.tinactory.content.gui.StorageMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.gui.StorageMenu.SLOT_SYNC;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageScreen<M extends InventoryMenu> extends MenuScreen<M> {
    public StorageScreen(M menu, Component title) {
        super(menu, title);
        this.contentHeight = menu.endY();

        var panel = new StoragePanel(this);
        rootPanel.addChild(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, PANEL_HEIGHT), panel);
        menu.onSyncPacket(SLOT_SYNC, panel::sync);
    }
}
