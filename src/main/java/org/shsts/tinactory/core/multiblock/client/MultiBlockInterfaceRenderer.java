package org.shsts.tinactory.core.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.shsts.tinactory.core.common.SmartBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockInterfaceRenderer implements BlockEntityRenderer<SmartBlockEntity> {
    public MultiBlockInterfaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SmartBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

    }
}
