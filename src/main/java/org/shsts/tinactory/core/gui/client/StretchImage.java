package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StretchImage extends ContainerWidget {
    private final Texture texture;
    private final Rect texRect;
    private final int border;

    public StretchImage(ContainerMenu<?> menu, RectD anchor, Rect offset,
                        Texture texture, Rect texRect, int border) {
        super(menu, anchor, offset);
        this.texture = texture;
        this.texRect = texRect;
        this.border = border;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        render(poseStack, this.texture, this.zIndex, this.rect, this.texRect, this.border);
    }

    public static void render(PoseStack poseStack, Texture texture, int zIndex,
                              Rect rect, Rect texRect, int border) {
        var w = rect.width() - border;
        var h = rect.height() - border;
        var tw = texRect.width() - border;
        var th = texRect.height() - border;

        var cR = rect.resize(border, border);
        var wR = rect.offset(border, 0).resize(w - border, border);
        var hR = rect.offset(0, border).resize(border, h - border);
        var mR = rect.offset(border, border).resize(w - border, h - border);
        var tR = texRect.resize(border, border);

        RenderUtil.blit(poseStack, texture, zIndex, cR, tR);
        RenderUtil.blit(poseStack, texture, zIndex, cR.offset(w, 0), tR.offset(tw, 0));
        RenderUtil.blit(poseStack, texture, zIndex, cR.offset(0, h), tR.offset(0, th));
        RenderUtil.blit(poseStack, texture, zIndex, cR.offset(w, h), tR.offset(tw, th));

        RenderUtil.blit(poseStack, texture, zIndex, wR, tR.offset(border, 0));
        RenderUtil.blit(poseStack, texture, zIndex, wR.offset(0, h), tR.offset(border, th));
        RenderUtil.blit(poseStack, texture, zIndex, hR, tR.offset(0, border));
        RenderUtil.blit(poseStack, texture, zIndex, hR.offset(w, 0), tR.offset(tw, border));

        RenderUtil.blit(poseStack, texture, zIndex, mR, tR.offset(tw - border, th - border));
    }
}
