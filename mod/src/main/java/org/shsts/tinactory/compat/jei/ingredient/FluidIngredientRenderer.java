package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidIngredientRenderer implements IIngredientRenderer<FluidStack> {
    @Override
    public void render(GuiGraphics graphics, FluidStack ingredient) {
        var rect = new Rect(0, 0, 16, 16);
        RenderUtil.renderFluidWithDecoration(graphics, ingredient, rect);
    }

    @Override
    public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
        return ClientUtil.fluidTooltip(ingredient, true);
    }
}
