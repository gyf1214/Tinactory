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
    public static IRenderable VOID = (poseStack, rect, z) -> {};

    public static IRenderable item(ItemStack stack) {
        return (poseStack, rect, z) -> RenderUtil.renderItem(stack, rect.x(), rect.y());
    }

    public static IRenderable fluid(FluidStack stack) {
        return (poseStack, rect, z) -> RenderUtil.renderFluid(poseStack, stack, rect, z);
    }

    public static IRenderable texture(Texture tex) {
        return (poseStack, rect, z) -> RenderUtil.blit(poseStack, tex, z, rect);
    }
}
