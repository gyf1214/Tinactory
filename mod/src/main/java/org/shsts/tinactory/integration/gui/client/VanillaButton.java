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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VanillaButton extends Button {
    private final Font font;
    private Component label;
    private int textWidth;
    private final Runnable onPress;

    public boolean disabled = false;

    public VanillaButton(MenuBase menu, Component label, @Nullable Component tooltip, Runnable onPress) {
        super(menu, tooltip);
        this.label = label;
        this.onPress = onPress;
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
        var texture = disabled ? Texture.VANILLA_BUTTON_DISABLED :
            isHovered(mouseX, mouseY) ? Texture.VANILLA_BUTTON_HOVERED : Texture.VANILLA_BUTTON;
        var w = rect.width() / 2;
        var rect1 = new Rect(rect.x(), rect.y(), w, rect.height());
        RenderUtil.blit(graphics, texture, rect1);
        RenderUtil.blit(graphics, texture, rect1.offset(w, 0), texture.width() - w, 0);

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
