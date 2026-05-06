package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.VanillaButton;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftCpuStatusPanel extends Panel {
    public AutocraftCpuStatusPanel(AutocraftTerminalScreen screen) {
        super(screen);
        var cancelButton = new VanillaButton(menu, new TextComponent("Cancel CPU"), null, screen::cancelCpuJob);

        addChild(RectD.corners(1d, 0d, 1d, 0d), new Rect(-88, 48, 84, 20), cancelButton);
    }

    public void onStatusSync(AutocraftCpuSyncPacket packet) {}
}
