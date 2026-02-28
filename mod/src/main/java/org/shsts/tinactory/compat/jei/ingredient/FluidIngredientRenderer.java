package org.shsts.tinactory.compat.jei.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidIngredientRenderer implements IIngredientRenderer<FluidStack> {
    @Override
    public void render(PoseStack poseStack, FluidStack ingredient) {
        var rect = new Rect(0, 0, 16, 16);
        RenderUtil.renderFluidWithDecoration(poseStack, ingredient, rect, 0);
    }

    @Override
    public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
        return ClientUtil.fluidTooltip(ingredient, true);
    }
}
