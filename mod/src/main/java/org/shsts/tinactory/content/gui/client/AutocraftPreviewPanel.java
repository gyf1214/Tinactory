package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.Widgets;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewPanel extends Panel {
    private final Label summary;

    public AutocraftPreviewPanel(AutocraftTerminalScreen screen) {
        super(screen);
        this.summary = new Label(menu, new TextComponent("No Preview"));
        var executeButton = Widgets.simpleButton(menu, new TextComponent("Execute"), null, screen::executePreview);
        var cancelButton = Widgets.simpleButton(menu, new TextComponent("Cancel"), null, screen::cancelPreview);

        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(4, 4, -4, 26), summary);
        addWidget(RectD.corners(0d, 1d, 0d, 1d), new Rect(4, -22, 68, 20), executeButton);
        addWidget(RectD.corners(1d, 1d, 1d, 1d), new Rect(-72, -22, 68, 20), cancelButton);
    }

    public void setSummary(Component component) {
        summary.setLine(0, component);
    }
}
