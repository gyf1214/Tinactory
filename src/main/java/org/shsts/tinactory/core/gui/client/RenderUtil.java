package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
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
        blit(poseStack, tex, zIndex, 0xFFFFFFFF, dstRect, srcRect);
    }

    private static void setGLColor(int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void blit(PoseStack poseStack, Texture tex, int zIndex, int color, Rect dstRect, Rect srcRect) {
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
        setGLColor(color);
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

    public static void blitAtlas(PoseStack poseStack, ResourceLocation atlas, TextureAtlasSprite sprite,
                                 int color, int zIndex, Rect dstRect) {

        var mat = poseStack.last().pose();
        var sx = (float) dstRect.x();
        var sy = (float) dstRect.y();
        var tx = (float) dstRect.endX();
        var ty = (float) dstRect.endY();
        var su = sprite.getU0();
        var sv = sprite.getV0();
        var tu = sprite.getU1();
        var tv = sprite.getV1();
        var zz = (float) zIndex;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        setGLColor(color);
        RenderSystem.setShaderTexture(0, atlas);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(mat, sx, ty, zz).uv(su, tv).endVertex();
        bufferbuilder.vertex(mat, tx, ty, zz).uv(tu, tv).endVertex();
        bufferbuilder.vertex(mat, tx, sy, zz).uv(tu, sv).endVertex();
        bufferbuilder.vertex(mat, sx, sy, zz).uv(su, sv).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

    public static void fill(PoseStack poseStack, Rect rect, int color) {
        GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.endX(), rect.endY(), color);
    }
}
