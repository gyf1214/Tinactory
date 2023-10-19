package org.shsts.tinactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public final class RenderUtil {
    public static void blit(PoseStack poseStack, Texture tex, int zIndex, int x, int y) {
        var width = tex.width();
        var height = tex.height();
        blit(poseStack, tex, zIndex, new Rect(x, y, width, height), new Rect(0, 0, width, height));
    }

    public static void blit(PoseStack poseStack, Texture tex, int zIndex, Rect dstRect) {
        blit(poseStack, tex, zIndex, dstRect, new Rect(0, 0, dstRect.width(), dstRect.height()));
    }

    public static void blit(PoseStack poseStack, Texture tex, int zIndex, Rect dstRect, int srcX, int srcY) {
        blit(poseStack, tex, zIndex, dstRect, new Rect(srcX, srcY, dstRect.width(), dstRect.height()));
    }

    public static void blit(PoseStack poseStack, Texture tex, int zIndex, Rect dstRect, Rect srcRect) {
        var mat = poseStack.last().pose();
        var sx = (float) dstRect.x();
        var sy = (float) dstRect.y();
        var tx = (float) dstRect.endX();
        var ty = (float) dstRect.endY();
        var su = (float) srcRect.x() / (float) tex.width();
        var sv = (float) srcRect.y() / (float) tex.height();
        var tu = (float) srcRect.endX() / (float) tex.width();
        var tv = (float) srcRect.endY() / (float) tex.height();
        var zz = (float) zIndex;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, tex.loc());
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(mat, sx, ty, zz).uv(su, tv).endVertex();
        bufferbuilder.vertex(mat, tx, ty, zz).uv(tu, tv).endVertex();
        bufferbuilder.vertex(mat, tx, sy, zz).uv(tu, sv).endVertex();
        bufferbuilder.vertex(mat, sx, sy, zz).uv(su, sv).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }
}
