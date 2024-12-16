package org.shsts.tinactory.core.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.multiblock.MultiBlockInterfaceBlock;

import java.util.Objects;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockInterfaceRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ModelBlockRenderer blockRenderer;

    public MultiBlockInterfaceRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
        this.blockRenderer = blockRenderDispatcher.getModelRenderer();
    }

    @Override
    public void render(BlockEntity be, float partialTick, PoseStack poseStack,
        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var blockState = be.getBlockState();
        if (!blockState.getValue(MultiBlockInterfaceBlock.JOINED)) {
            return;
        }
        var world = be.getLevel();
        assert world != null;
        var pos = be.getBlockPos();
        var cap = MACHINE.tryGet(be);
        if (cap.isEmpty() || !(cap.get() instanceof MultiBlockInterface multiBlockInterface)) {
            return;
        }
        var appearanceBlock = multiBlockInterface.getAppearanceBlock();

        var random = world.getRandom();
        var seed = blockState.getSeed(pos);
        var modelData = Objects.requireNonNullElse(ModelDataManager.getModelData(world, pos),
            EmptyModelData.INSTANCE);
        var vertexConsumer = bufferSource.getBuffer(RenderType.cutoutMipped());

        if (appearanceBlock.isPresent()) {
            var appearanceModel = blockRenderDispatcher.getBlockModel(appearanceBlock.get());
            var overlayModel = blockRenderDispatcher.getBlockModel(blockState);

            blockRenderer.tesselateBlock(world, appearanceModel, appearanceBlock.get(), pos,
                poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
            blockRenderer.tesselateBlock(world, overlayModel, blockState, pos,
                poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
        } else {
            var model = blockRenderDispatcher.getBlockModel(
                blockState.setValue(MultiBlockInterfaceBlock.JOINED, false));

            blockRenderer.tesselateBlock(world, model, blockState, pos,
                poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
        }
    }
}
