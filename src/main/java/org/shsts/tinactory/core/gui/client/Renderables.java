package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Texture;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Renderables {
    public static IRectRenderable VOID = (poseStack, rect, z) -> {};

    public static IRectRenderable item(ItemStack stack) {
        return (poseStack, rect, z) -> {
            var poseStack1 = RenderUtil.applyToModelViewStack(poseStack);
            RenderUtil.renderItem(stack, rect.x(), rect.y());
            RenderUtil.popModelViewStack(poseStack1);
        };
    }

    public static IRectRenderable fluid(FluidStack stack) {
        return (poseStack, rect, z) -> RenderUtil.renderFluid(poseStack, stack, rect, z);
    }

    public static IRectRenderable texture(Texture tex) {
        return (poseStack, rect, z) -> RenderUtil.blit(poseStack, tex, z, rect);
    }
}
