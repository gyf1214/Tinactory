package org.shsts.tinactory.integration.jei.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.RenderUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidIngredientRenderer implements IIngredientRenderer<FluidStackWrapper> {
    @Override
    public void render(PoseStack poseStack, FluidStackWrapper ingredient) {
        var rect = new Rect(0, 0, 16, 16);
        RenderUtil.renderFluidWithDecoration(poseStack, ingredient.stack(), rect, 0);
    }

    @Override
    public List<Component> getTooltip(FluidStackWrapper ingredient, TooltipFlag tooltipFlag) {
        return RenderUtil.fluidTooltip(ingredient.stack());
    }
}
