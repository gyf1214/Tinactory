package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class RenderUtil {
    private static final int CYCLE_TIME = 1000;
    public static final int TEXT_COLOR = 0xFF404040;

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
        float a = (float) (color >> 24 & 255) / 255f;
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, tex.loc());
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(mat, sx, ty, zz).uv(su, tv).endVertex();
        bufferbuilder.vertex(mat, tx, ty, zz).uv(tu, tv).endVertex();
        bufferbuilder.vertex(mat, tx, sy, zz).uv(tu, sv).endVertex();
        bufferbuilder.vertex(mat, sx, sy, zz).uv(su, sv).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.disableBlend();
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, atlas);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(mat, sx, ty, zz).uv(su, tv).endVertex();
        bufferbuilder.vertex(mat, tx, ty, zz).uv(tu, tv).endVertex();
        bufferbuilder.vertex(mat, tx, sy, zz).uv(tu, sv).endVertex();
        bufferbuilder.vertex(mat, sx, sy, zz).uv(su, sv).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.disableBlend();
    }

    public static int mixColor(int color1, int color2) {
        var a1 = (color1 >> 24) & 0xFF;
        var r1 = (color1 >> 16) & 0xFF;
        var g1 = (color1 >> 8) & 0xFF;
        var b1 = color1 & 0xFF;

        var a2 = (color2 >> 24) & 0xFF;
        var r2 = (color2 >> 16) & 0xFF;
        var g2 = (color2 >> 8) & 0xFF;
        var b2 = color2 & 0xFF;

        var a = a1 * a2 / 0xFF;
        var r = r1 * r2 / 0xFF;
        var g = g1 * g2 / 0xFF;
        var b = b1 * b2 / 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void renderFluid(PoseStack poseStack, FluidStack stack, Rect rect, int color, int zIndex) {
        var fluid = stack.getFluid();
        if (!stack.isEmpty() && fluid != null) {
            var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            var attribute = fluid.getAttributes();
            var sprite = atlas.apply(attribute.getStillTexture());
            var renderColor = mixColor(attribute.getColor(), color);
            RenderUtil.blitAtlas(poseStack, InventoryMenu.BLOCK_ATLAS, sprite, renderColor, zIndex, rect);
        }
    }

    public static void renderFluid(PoseStack poseStack, FluidStack stack, Rect rect, int zIndex) {
        renderFluid(poseStack, stack, rect, 0xFFFFFFFF, zIndex);
    }

    public static void renderFluid(PoseStack poseStack, FluidStack stack, int x, int y, int zIndex) {
        renderFluid(poseStack, stack, new Rect(x, y, 16, 16), 0xFFFFFFFF, zIndex);
    }

    public static void renderItem(ItemStack stack, int x, int y) {
        ClientUtil.getItemRenderer().renderAndDecorateFakeItem(stack, x, y);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderIngredient(IProcessingObject ingredient, Consumer<ItemStack> itemRenderer,
                                        Consumer<FluidStack> fluidRenderer) {

        if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            itemRenderer.accept(item.stack());
        } else if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase item) {
            var items = item.ingredient.getItems();
            if (items.length > 0) {
                var cycle = System.currentTimeMillis() / CYCLE_TIME;
                var idx = (int) (cycle % items.length);
                itemRenderer.accept(items[idx]);
            }
        } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
            fluidRenderer.accept(fluid.fluid());
        } else if (ingredient instanceof ProcessingResults.ItemResult item) {
            itemRenderer.accept(item.stack);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluid) {
            fluidRenderer.accept(fluid.stack);
        }
    }

    public static void fill(PoseStack poseStack, Rect rect, int color) {
        GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.endX(), rect.endY(), color);
    }

    public static void renderText(PoseStack poseStack, Component text, int x, int y, int color) {
        ClientUtil.getFont().draw(poseStack, text, (float) x, (float) y, color);
    }

    public static void renderText(PoseStack poseStack, Component text, int x, int y) {
        renderText(poseStack, text, x, y, TEXT_COLOR);
    }
}
