package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
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

    public int color = RenderUtil.TEXT_COLOR;
    public Alignment horizontalAlign = Alignment.BEGIN;
    public Alignment verticalAlign = Alignment.BEGIN;

    public Label(Menu<?> menu, RectD anchor, Rect offset) {
        super(menu, anchor, offset);
    }

    public Label(Menu<?> menu, Rect rect) {
        super(menu, rect);
    }

    public void setText(Component value) {
        text = value;
        cacheWidth = ClientUtil.getFont().width(value);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var font = ClientUtil.getFont();
        int x = rect.inX(horizontalAlign.value) - (int) (cacheWidth * horizontalAlign.value);
        int y = rect.inY(verticalAlign.value) - (int) (font.lineHeight * verticalAlign.value);
        RenderUtil.renderText(poseStack, text, x, y, color);
    }
}
