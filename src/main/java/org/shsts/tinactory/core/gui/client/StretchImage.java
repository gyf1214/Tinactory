package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.IMenu;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StretchImage extends MenuWidget {
    private final Texture texture;
    private final Rect texRect;
    private final int border;

    public StretchImage(IMenu menu, Texture texture, Rect texRect, int border) {
        super(menu);
        this.texture = texture;
        this.texRect = texRect;
        this.border = border;
    }

    public StretchImage(Menu<?, ?> menu, Texture texture, Rect texRect, int border) {
        super(menu);
        this.texture = texture;
        this.texRect = texRect;
        this.border = border;
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        render(poseStack, texture, getBlitOffset(), rect, texRect, border);
    }

    public static void render(PoseStack poseStack, Texture texture, int zIndex,
        Rect rect, Rect texRect, int border) {
        render(poseStack, texture, zIndex, RenderUtil.WHITE, rect, texRect, border);
    }

    public static void render(PoseStack poseStack, Texture texture, int zIndex,
        int color, Rect rect, Rect texRect, int border) {
        var w = rect.width() - border;
        var h = rect.height() - border;
        var tw = texRect.width() - border;
        var th = texRect.height() - border;

        var cR = rect.resize(border, border);
        var wR = rect.offset(border, 0).resize(w - border, border);
        var hR = rect.offset(0, border).resize(border, h - border);
        var mR = rect.offset(border, border).resize(w - border, h - border);
        var tR = texRect.resize(border, border);

        RenderUtil.blit(poseStack, texture, zIndex, color, cR, tR);
        RenderUtil.blit(poseStack, texture, zIndex, color, cR.offset(w, 0), tR.offset(tw, 0));
        RenderUtil.blit(poseStack, texture, zIndex, color, cR.offset(0, h), tR.offset(0, th));
        RenderUtil.blit(poseStack, texture, zIndex, color, cR.offset(w, h), tR.offset(tw, th));

        RenderUtil.blit(poseStack, texture, zIndex, color, wR, tR.offset(border, 0));
        RenderUtil.blit(poseStack, texture, zIndex, color, wR.offset(0, h), tR.offset(border, th));
        RenderUtil.blit(poseStack, texture, zIndex, color, hR, tR.offset(0, border));
        RenderUtil.blit(poseStack, texture, zIndex, color, hR.offset(w, 0), tR.offset(tw, border));

        RenderUtil.blit(poseStack, texture, zIndex, color, mR, tR.offset(tw - border, th - border));
    }
}
