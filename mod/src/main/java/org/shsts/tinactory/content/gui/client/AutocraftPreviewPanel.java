package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.Widgets;

import static org.shsts.tinactory.content.gui.client.AutocraftTerminalScreen.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewPanel extends Panel {
    public AutocraftPreviewPanel(AutocraftTerminalScreen screen) {
        super(screen);
        var executeButton = Widgets.simpleButton(menu, tr("execute"), null, screen::executePreview);
        var cancelButton = Widgets.simpleButton(menu, tr("cancel"), null, screen::cancelPreview);

        addChild(RectD.corners(0d, 1d, 0d, 1d), new Rect(4, -22, 68, 20), executeButton);
        addChild(RectD.corners(1d, 1d, 1d, 1d), new Rect(-72, -22, 68, 20), cancelButton);
    }

    public void onPreviewSync(AutocraftPreviewSyncPacket packet) {}
}
