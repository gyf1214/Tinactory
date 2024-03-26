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
        var tex = this.texture;

        var b = this.border;
        var w = this.rect.width() - b;
        var h = this.rect.height() - b;
        var tw = this.texRect.width() - b;
        var th = this.texRect.height() - b;

        var cR = this.rect.resize(b, b);
        var wR = this.rect.offset(b, 0).resize(w - b, b);
        var hR = this.rect.offset(0, b).resize(b, h - b);
        var mR = this.rect.offset(b, b).resize(w - b, h - b);
        var tR = this.texRect.resize(b, b);

        RenderUtil.blit(poseStack, tex, this.zIndex, cR, tR);
        RenderUtil.blit(poseStack, tex, this.zIndex, cR.offset(w, 0), tR.offset(tw, 0));
        RenderUtil.blit(poseStack, tex, this.zIndex, cR.offset(0, h), tR.offset(0, th));
        RenderUtil.blit(poseStack, tex, this.zIndex, cR.offset(w, h), tR.offset(tw, th));

        RenderUtil.blit(poseStack, tex, this.zIndex, wR, tR.offset(b, 0));
        RenderUtil.blit(poseStack, tex, this.zIndex, wR.offset(0, h), tR.offset(b, th));
        RenderUtil.blit(poseStack, tex, this.zIndex, hR, tR.offset(0, b));
        RenderUtil.blit(poseStack, tex, this.zIndex, hR.offset(w, 0), tR.offset(tw, b));

        RenderUtil.blit(poseStack, tex, this.zIndex, mR, tR.offset(tw - b, th - b));
    }
}
