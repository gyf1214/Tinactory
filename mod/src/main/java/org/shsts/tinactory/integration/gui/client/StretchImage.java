package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.MenuBase;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StretchImage extends MenuWidget {
    private final Texture texture;
    private final Rect texRect;
    private final int border;

    public StretchImage(MenuBase menu, Texture texture, Rect texRect, int border) {
        super(menu);
        this.texture = texture;
        this.texRect = texRect;
        this.border = border;
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        render(graphics, texture, rect(), texRect, border);
    }

    public static void render(GuiGraphics graphics, Texture texture, Rect rect, Rect texRect, int border) {
        render(graphics, texture, RenderUtil.WHITE, rect, texRect, border);
    }

    public static void render(GuiGraphics graphics, Texture texture, int color, Rect rect, Rect texRect, int border) {
        var w = rect.width() - border;
        var h = rect.height() - border;
        var tw = texRect.width() - border;
        var th = texRect.height() - border;

        var cR = rect.resize(border, border);
        var wR = rect.offset(border, 0).resize(w - border, border);
        var hR = rect.offset(0, border).resize(border, h - border);
        var mR = rect.offset(border, border).resize(w - border, h - border);
        var tR = texRect.resize(border, border);

        RenderUtil.blit(graphics, texture, color, cR, tR);
        RenderUtil.blit(graphics, texture, color, cR.offset(w, 0), tR.offset(tw, 0));
        RenderUtil.blit(graphics, texture, color, cR.offset(0, h), tR.offset(0, th));
        RenderUtil.blit(graphics, texture, color, cR.offset(w, h), tR.offset(tw, th));

        RenderUtil.blit(graphics, texture, color, wR, tR.offset(border, 0));
        RenderUtil.blit(graphics, texture, color, wR.offset(0, h), tR.offset(border, th));
        RenderUtil.blit(graphics, texture, color, hR, tR.offset(0, border));
        RenderUtil.blit(graphics, texture, color, hR.offset(w, 0), tR.offset(tw, border));

        RenderUtil.blit(graphics, texture, color, mR, tR.offset(tw - border, th - border));
    }
}
