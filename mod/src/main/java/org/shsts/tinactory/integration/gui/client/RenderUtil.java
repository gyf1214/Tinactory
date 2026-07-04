package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.TextureRenderDescriptor;
import org.shsts.tinactory.integration.util.ClientUtil;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class RenderUtil {
    public static final int TEXT_COLOR = 0xFF404040;
    public static final int HIGHLIGHT_COLOR = 0x80FFFFFF;
    public static final int WHITE = 0xFFFFFFFF;

    public static void blit(GuiGraphics graphics, Texture tex, Rect dstRect) {
        blit(graphics, tex, dstRect, new Rect(0, 0, dstRect.width(), dstRect.height()));
    }

    public static void blit(GuiGraphics graphics, Texture tex, Rect dstRect, int srcX, int srcY) {
        blit(graphics, tex, dstRect, new Rect(srcX, srcY, dstRect.width(), dstRect.height()));
    }

    public static void blit(GuiGraphics graphics, Texture tex, Rect dstRect, Rect srcRect) {
        blit(graphics, tex, WHITE, dstRect, srcRect);
    }

    private static float colorComponent(int color, int shift) {
        return (float) (color >> shift & 255) / 255f;
    }

    private static void setColor(GuiGraphics graphics, int color) {
        graphics.setColor(colorComponent(color, 16), colorComponent(color, 8),
            colorComponent(color, 0), colorComponent(color, 24));
    }

    public static void blit(GuiGraphics graphics, Texture tex, int color, Rect dstRect, Rect srcRect) {
        setColor(graphics, color);
        graphics.blit(tex.loc(), dstRect.x(), dstRect.y(), srcRect.x(), srcRect.y(),
            dstRect.width(), dstRect.height(), tex.width(), tex.height());
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    public static void blitAtlas(GuiGraphics graphics, TextureAtlasSprite sprite, int color, Rect dstRect) {
        setColor(graphics, color);
        graphics.blit(dstRect.x(), dstRect.y(), dstRect.width(), dstRect.height(), 0, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
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

    public static void renderSlotHover(GuiGraphics graphics, Rect rect) {
        RenderUtil.fill(graphics, rect, HIGHLIGHT_COLOR);
    }

    public static void renderFluid(GuiGraphics graphics, FluidStack stack, Rect rect, int color) {
        if (!stack.isEmpty()) {
            var fluid = stack.getFluid();
            var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            var extension = IClientFluidTypeExtensions.of(fluid);
            var sprite = atlas.apply(extension.getStillTexture(stack));
            var renderColor = mixColor(extension.getTintColor(stack), color);
            blitAtlas(graphics, sprite, renderColor, rect);
        }
    }

    public static void renderFluid(GuiGraphics graphics, FluidStack stack, Rect rect) {
        renderFluid(graphics, stack, rect, WHITE);
    }

    public static void renderFluid(GuiGraphics graphics, FluidStack stack, int x, int y) {
        renderFluid(graphics, stack, new Rect(x, y, 16, 16), WHITE);
    }

    public static void renderFluidWithDecoration(GuiGraphics graphics, FluidStack stack, Rect rect) {
        if (!stack.isEmpty()) {
            renderFluid(graphics, stack, rect);
            var s = ClientUtil.getFluidAmountString(stack.getAmount());
            var font = ClientUtil.getFont();
            var x = rect.endX() + 1 - font.width(s);
            var y = rect.endY() + 2 - font.lineHeight;
            graphics.drawString(font, s, x, y, 0xFFFFFFFF, true);
        }
    }

    public static void renderGhostFluid(GuiGraphics graphics, FluidStack stack, Rect rect) {
        renderFluid(graphics, stack, rect, 0x55FFFFFF);
    }

    /**
     * Non-GUI block rendering helper for block-entity renderers. Keeps the explicit PoseStack boundary.
     */
    public static void renderBlockInWorld(ModelBlockRenderer renderer, Level world, BakedModel model,
        BlockState blockState, BlockPos pos, PoseStack poseStack, VertexConsumer vertexConsumer,
        boolean checkSides, RandomSource random, long seed, int packedOverlay, ModelData modelData) {
        poseStack.pushPose();
        renderer.tesselateBlock(world, model, blockState, pos, poseStack, vertexConsumer,
            checkSides, random, seed, packedOverlay, modelData, RenderType.translucent());
        poseStack.popPose();
    }

    private static void renderBlockQuad(BakedQuad quad, BlockColors blockColors, BlockState blockState,
        PoseStack.Pose pose, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        var color = quad.isTinted() ? blockColors.getColor(blockState, null, null, quad.getTintIndex()) :
            0xFFFFFFFF;
        var r = (float) (color >> 16 & 255) / 255.0F;
        var g = (float) (color >> 8 & 255) / 255.0F;
        var b = (float) (color & 255) / 255.0F;
        vertexConsumer.putBulkData(pose, quad, r, g, b, 1f, packedLight, packedOverlay);
    }

    /**
     * Non-GUI block rendering helper for detached or virtual-world block models.
     */
    public static void renderBlockModel(BakedModel model, BlockState blockState,
        PoseStack poseStack, VertexConsumer vertexConsumer, RandomSource random,
        int packedLight, int packedOverlay) {
        var blockColors = Minecraft.getInstance().getBlockColors();
        var pose = poseStack.last();

        // we don't consider cull, model data, or render type
        for (var dir : Direction.values()) {
            var quads = model.getQuads(blockState, dir, random, ModelData.EMPTY, RenderType.translucent());
            for (var quad : quads) {
                renderBlockQuad(quad, blockColors, blockState, pose, vertexConsumer,
                    packedLight, packedOverlay);
            }
        }
        for (var quad : model.getQuads(blockState, null, random, ModelData.EMPTY, RenderType.translucent())) {
            renderBlockQuad(quad, blockColors, blockState, pose, vertexConsumer,
                packedLight, packedOverlay);
        }
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
    }

    public static void renderItemWithDecoration(GuiGraphics graphics, ItemStack stack, int x, int y) {
        var text = ClientUtil.getItemCountString(stack.getCount());
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(ClientUtil.getFont(), stack, x, y, text);
    }

    public static void renderFakeItemWithDecoration(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderFakeItem(stack, x, y);
        graphics.renderItemDecorations(ClientUtil.getFont(), stack, x, y);
    }

    public static void renderGhostItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        RenderUtil.fill(graphics, new Rect(x, y, 16, 16), 0xAA8B8B8B);
    }

    public static void renderDescriptor(GuiGraphics graphics, IRenderDescriptor descriptor, Rect rect) {
        switch (descriptor) {
            case EmptyRenderDescriptor ignored -> {}
            case TextureRenderDescriptor(Texture texture) -> blit(graphics, texture, rect);
            case ItemIdRenderDescriptor(ResourceLocation id) -> BuiltInRegistries.ITEM.getOptional(id)
                .ifPresent(item -> renderItem(graphics, new ItemStack(item), rect.x(), rect.y()));
            case ItemRenderDescriptor(ItemStack stack) -> renderItem(graphics, stack, rect.x(), rect.y());
            case FluidRenderDescriptor(FluidStack stack) -> renderFluid(graphics, stack, rect.x(), rect.y());
            default -> throw new IllegalArgumentException(
                "Unsupported render descriptor: " + descriptor.getClass().getName());
        }
    }

    public static void renderGhostDescriptor(GuiGraphics graphics, IRenderDescriptor descriptor, Rect rect) {
        switch (descriptor) {
            case EmptyRenderDescriptor ignored -> {}
            case TextureRenderDescriptor(Texture texture) -> blit(graphics, texture, rect);
            case ItemIdRenderDescriptor(ResourceLocation id) -> BuiltInRegistries.ITEM.getOptional(id)
                .ifPresent(item -> renderGhostItem(graphics, new ItemStack(item), rect.x(), rect.y()));
            case ItemRenderDescriptor(ItemStack stack) -> renderGhostItem(graphics, stack, rect.x(), rect.y());
            case FluidRenderDescriptor(FluidStack stack) -> renderGhostFluid(graphics, stack, rect);
            default -> throw new IllegalArgumentException(
                "Unsupported render descriptor: " + descriptor.getClass().getName());
        }
    }

    public static void fill(GuiGraphics graphics, Rect rect, int color) {
        graphics.fill(rect.x(), rect.y(), rect.endX(), rect.endY(), color);
    }

    public static void renderText(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(ClientUtil.getFont(), text, x, y, color);
    }

    public static void renderText(GuiGraphics graphics, Component text, int x, int y, int color, float scale) {
        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0d);
        poseStack.scale(scale, scale, 1f);
        graphics.drawString(ClientUtil.getFont(), text, 0, 0, color);
        poseStack.popPose();
    }

    public static void renderText(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color) {
        graphics.drawString(ClientUtil.getFont(), text, x, y, color);
    }

    public static void renderText(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color,
        float scale) {
        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0d);
        poseStack.scale(scale, scale, 1f);
        graphics.drawString(ClientUtil.getFont(), text, 0, 0, color);
        poseStack.popPose();
    }

    public static void renderText(GuiGraphics graphics, Component text, int x, int y) {
        renderText(graphics, text, x, y, TEXT_COLOR);
    }

    public static void renderText(GuiGraphics graphics, Component text, int x, int y, float scale) {
        renderText(graphics, text, x, y, TEXT_COLOR, scale);
    }
}
