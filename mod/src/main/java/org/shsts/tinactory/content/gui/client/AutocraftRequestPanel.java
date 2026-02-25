package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.Widgets;

import java.util.OptionalLong;

import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftRequestPanel extends Panel {
    private final Label title;
    private final EditBox quantityInput;

    public AutocraftRequestPanel(AutocraftTerminalScreen screen) {
        super(screen);
        this.title = new Label(menu, new TextComponent("Autocraft Request"));
        this.quantityInput = Widgets.editBox();
        quantityInput.setValue("1");
        var previewButton = Widgets.simpleButton(menu, new TextComponent("Preview"), null, screen::requestPreview);

        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(4, 4, -4, 12), title);
        addWidget(RectD.corners(0d, 0d, 0d, 0d), new Rect(4, 22, 72, EDIT_HEIGHT), quantityInput);
        addWidget(RectD.corners(1d, 0d, 1d, 0d), new Rect(-72, 20, 68, 20), previewButton);
    }

    public OptionalLong quantity() {
        try {
            return OptionalLong.of(Long.parseLong(quantityInput.getValue()));
        } catch (NumberFormatException ignored) {
            return OptionalLong.empty();
        }
    }

    public void setTitle(Component component) {
        title.setLine(0, component);
    }
}
