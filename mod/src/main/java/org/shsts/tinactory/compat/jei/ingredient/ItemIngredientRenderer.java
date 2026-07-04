package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemIngredientRenderer implements IIngredientRenderer<ItemStack> {
    @Override
    public void render(GuiGraphics graphics, ItemStack ingredient) {
        RenderUtil.renderFakeItemWithDecoration(graphics, ingredient, 0, 0);
    }

    @Override
    public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
        return new ArrayList<>(ClientUtil.itemTooltip(ingredient, tooltipFlag));
    }

    @Override
    public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        var renderProperties = IClientItemExtensions.of(ingredient);
        var fontRenderer = renderProperties.getFont(ingredient, IClientItemExtensions.FontContext.TOOLTIP);
        if (fontRenderer != null) {
            return fontRenderer;
        }
        return minecraft.font;
    }
}
