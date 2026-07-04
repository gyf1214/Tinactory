package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VanillaButton extends Button {
    private final Texture texture;
    private final Font font;
    private Component label;
    private int textWidth;
    private final Runnable onPress;

    public boolean disabled = false;

    public VanillaButton(MenuBase menu, Component label, @Nullable Component tooltip, Runnable onPress) {
        super(menu, tooltip);
        this.label = label;
        this.onPress = onPress;
        texture = Texture.VANILLA_WIDGETS;
        font = ClientUtil.getFont();
        textWidth = font.width(label);
    }

    public void setLabel(Component val) {
        label = val;
        textWidth = font.width(val);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var rect = rect();
        int y;
        y = disabled ? 66 - BUTTON_HEIGHT : (isHovered(mouseX, mouseY) ? 66 + BUTTON_HEIGHT : 66);
        var w = rect.width() / 2;
        var rect1 = new Rect(rect.x(), rect.y(), w, rect.height());
        RenderUtil.blit(graphics, texture, rect1, 0, y);
        RenderUtil.blit(graphics, texture, rect1.offset(w, 0), 200 - w, y);

        graphics.drawString(font, label, rect.x() + w - textWidth / 2,
            rect.y() + (rect.height() - font.lineHeight) / 2, RenderUtil.WHITE, true);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        if (!disabled) {
            super.onMouseClicked(mouseX, mouseY, button);
            onPress.run();
        }
    }
}
