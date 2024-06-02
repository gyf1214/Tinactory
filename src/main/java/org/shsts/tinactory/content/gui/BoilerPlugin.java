package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private final int syncSlot;

    public BoilerPlugin(M menu) {
        this.syncSlot = menu.addSyncSlot(MenuSyncPacket.Double::new,
                be -> ((Boiler) AllCapabilities.PROCESSOR.get(be)).getHeat());
    }

    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        var menu = screen.getMenu();
        var label = new Label(menu);
        label.verticalAlign = Label.Alignment.END;
        menu.<MenuSyncPacket.Double>onSyncPacket(syncSlot,
                p -> label.setLine(0, new TextComponent("Heat: %.2f".formatted(p.getData()))));

        screen.addWidget(RectD.corners(0d, 1d, 0d, 1d), Rect.ZERO, label);
    }
}
