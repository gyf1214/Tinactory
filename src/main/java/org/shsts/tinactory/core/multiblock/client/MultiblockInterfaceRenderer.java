package org.shsts.tinactory.core.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.slf4j.Logger;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.core.gui.client.RenderUtil.renderBlockInWorld;
import static org.shsts.tinactory.core.gui.client.RenderUtil.renderBlockModel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockInterfaceRenderer implements BlockEntityRenderer<BlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ModelBlockRenderer blockRenderer;

    public MultiblockInterfaceRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
        this.blockRenderer = blockRenderDispatcher.getModelRenderer();
    }

    @Override
    public void render(BlockEntity be, float partialTick, PoseStack poseStack,
        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        try {
            var blockState = be.getBlockState();
            if (!blockState.getValue(MultiblockInterfaceBlock.JOINED)) {
                return;
            }
            var world = be.getLevel();
            assert world != null;
            var pos = be.getBlockPos();
            var cap = MACHINE.tryGet(be);
            if (cap.isEmpty() || !(cap.get() instanceof MultiblockInterface multiblockInterface)) {
                return;
            }
            var appearanceBlock = multiblockInterface.getAppearanceBlock();

            var random = world.getRandom();
            var seed = blockState.getSeed(pos);
            // in litematica, the schematic is rendered in a virtual world, which does not work with forge
            // ModelData. We don't use ModelData anyway but might need a way make sure this works in the future.
            var modelData = EmptyModelData.INSTANCE;
            // use translucent for best compatibility
            var vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
            // if the world is not the client world (e.g. litematica schematic world), we render without regard to it.
            var renderInWorld = world == Minecraft.getInstance().level;

            if (appearanceBlock.isPresent()) {
                var appearance = appearanceBlock.get();
                var appearanceModel = blockRenderDispatcher.getBlockModel(appearance);
                var overlayModel = blockRenderDispatcher.getBlockModel(blockState);

                if (renderInWorld) {
                    // TODO: it's not very clear whether we really need this call
                    renderBlockInWorld(blockRenderer, world, appearanceModel, appearance, pos,
                        poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
                    renderBlockInWorld(blockRenderer, world, overlayModel, blockState, pos,
                        poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
                } else {
                    renderBlockModel(appearanceModel, appearance, poseStack, vertexConsumer, random,
                        packedLight, packedOverlay);
                    renderBlockModel(overlayModel, blockState, poseStack, vertexConsumer, random,
                        packedLight, packedOverlay);
                }
            } else {
                var blockState1 = blockState.setValue(MultiblockInterfaceBlock.JOINED, false);
                var model = blockRenderDispatcher.getBlockModel(blockState1);

                if (renderInWorld) {
                    renderBlockInWorld(blockRenderer, world, model, blockState1, pos,
                        poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
                } else {
                    renderBlockModel(model, blockState1, poseStack, vertexConsumer, random,
                        packedLight, packedOverlay);
                }
            }
        } catch (Throwable e) {
            // litematica does not log rendering error so we log here to have some idea.
            LOGGER.error("Error rendering multiblock interface!", e);
            throw e;
        }
    }
}
