package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Label extends MenuWidget {
    public enum Alignment {
        BEGIN(0d), MIDDLE(0.5d), END(1d);

        Alignment(double value) {
            this.value = value;
        }

        private final double value;
    }

    private Component text = TextComponent.EMPTY;
    private int cacheWidth = 0;
    private final Font font = ClientUtil.getFont();

    public int color = RenderUtil.TEXT_COLOR;
    public Alignment horizontalAlign = Alignment.BEGIN;
    public Alignment verticalAlign = Alignment.BEGIN;

    public Label(Menu<?> menu) {
        super(menu);
    }

    public Label(Menu<?> menu, Alignment horizontalAlign, Component text) {
        super(menu);
        this.horizontalAlign = horizontalAlign;
        setText(text);
    }

    public void setText(Component value) {
        text = value;
        cacheWidth = font.width(value);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int x = rect.inX(horizontalAlign.value) - (int) (cacheWidth * horizontalAlign.value);
        int y = rect.inY(verticalAlign.value) - (int) (font.lineHeight * verticalAlign.value);
        RenderUtil.renderText(poseStack, text, x, y, color);
    }
}
