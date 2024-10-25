package org.shsts.tinactory.core.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.multiblock.MultiBlockInterfaceBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockInterfaceRenderer implements BlockEntityRenderer<SmartBlockEntity> {
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ModelBlockRenderer blockRenderer;

    public MultiBlockInterfaceRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
        this.blockRenderer = blockRenderDispatcher.getModelRenderer();
    }

    @Override
    public void render(SmartBlockEntity sbe, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var blockState = sbe.getBlockState();
        if (!blockState.getValue(MultiBlockInterfaceBlock.JOINED)) {
            return;
        }
        var world = sbe.getLevel();
        assert world != null;
        var pos = sbe.getBlockPos();
        var cap = AllCapabilities.MACHINE.tryGet(sbe);
        if (cap.isEmpty() || !(cap.get() instanceof MultiBlockInterface multiBlockInterface)) {
            return;
        }
        var appearanceBlock = multiBlockInterface.getAppearanceBlock()
                .orElse(Blocks.AIR.defaultBlockState());

        var random = world.getRandom();
        var seed = blockState.getSeed(pos);
        var modelData = Objects.requireNonNullElse(ModelDataManager.getModelData(world, pos),
                EmptyModelData.INSTANCE);
        var vertexConsumer = bufferSource.getBuffer(RenderType.cutoutMipped());
        var appearanceModel = blockRenderDispatcher.getBlockModel(appearanceBlock);
        var overlayModel = blockRenderDispatcher.getBlockModel(blockState);

        blockRenderer.tesselateBlock(world, appearanceModel, appearanceBlock, pos,
                poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
        blockRenderer.tesselateBlock(world, overlayModel, blockState, pos,
                poseStack, vertexConsumer, true, random, seed, packedOverlay, modelData);
    }
}
