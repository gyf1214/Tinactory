package org.shsts.tinactory.compat.jei.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.IdentityHashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockStructureRenderer {
    private static final float YAW = -45f;
    private static final float PITCH = 30f;
    private static final float WIDTH_SCALE = (float) Math.sqrt(2) / 2;
    private static final float HEIGHT_SCALE1 = (float) Math.sqrt(3) / 2;
    private static final float HEIGHT_SCALE2 = WIDTH_SCALE / 2f;
    private final Map<IMultiblockDisplay, Map<IBlockIngredient, BlockState>> stateCache;

    public MultiblockStructureRenderer() {
        stateCache = new IdentityHashMap<>();
    }

    private Map<IBlockIngredient, BlockState> states(IMultiblockDisplay display) {
        return stateCache.computeIfAbsent(display, this::createStates);
    }

    private Map<IBlockIngredient, BlockState> createStates(IMultiblockDisplay display) {
        var states = new IdentityHashMap<IBlockIngredient, BlockState>();
        for (var y = 0; y < display.height(); y++) {
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    display.getIngredient(x, y, z).ifPresent(ingredient ->
                        states.computeIfAbsent(ingredient, $ -> $.display(ClientUtil.registryAccess())));
                }
            }
        }
        return states;
    }

    private static BlockState controllerState(MultiblockSet set) {
        var state = set.controller().get().defaultBlockState();
        if (state.hasProperty(PrimitiveBlock.FACING)) {
            state = state.setValue(PrimitiveBlock.FACING, Direction.SOUTH);
        }
        return state;
    }

    private static float scale(IMultiblockDisplay display, Rect rect) {
        var wd = display.width() + display.depth();
        var width = wd * WIDTH_SCALE;
        var height = display.height() * HEIGHT_SCALE1 + wd * HEIGHT_SCALE2;
        return Math.min(rect.width() / width, rect.height() / height);
    }

    private static void renderState(PoseStack poseStack, GuiGraphics graphics, BlockState state,
        int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        RenderUtil.renderBlockInGui(poseStack, graphics.bufferSource(), state,
            LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    public void render(GuiGraphics graphics, MultiblockSet set, Rect rect) {
        var display = set.display();
        var states = states(display);
        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(rect.x() + rect.width() / 2f, rect.y() + rect.height() / 2f, 100f);
        var scale = scale(display, rect);
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(PITCH));
        poseStack.mulPose(Axis.YP.rotationDegrees(YAW));
        poseStack.translate(-display.width() / 2f, -display.height() / 2f, -display.depth() / 2f);

        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        for (var y = 0; y < display.height(); y++) {
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    var state = display.getIngredient(x, y, z).map(states::get).orElse(null);
                    if (state != null) {
                        renderState(poseStack, graphics, state, x, y, z);
                    }
                }
            }
        }
        var controller = display.controllerPosition();
        renderState(poseStack, graphics, controllerState(set), controller.getX(), controller.getY(),
            controller.getZ());
        graphics.flush();
        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        poseStack.popPose();
    }
}
