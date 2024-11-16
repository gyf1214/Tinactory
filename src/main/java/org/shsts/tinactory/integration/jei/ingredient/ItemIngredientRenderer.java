package org.shsts.tinactory.integration.jei.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.RenderProperties;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemIngredientRenderer implements IIngredientRenderer<ItemStack> {
    @Override
    public void render(PoseStack poseStack, ItemStack ingredient) {
        var poseStack1 = RenderUtil.applyToModelViewStack(poseStack);
        RenderUtil.renderFakeItemWithDecoration(ingredient, 0, 0, null);
        RenderUtil.popModelViewStack(poseStack1);
    }

    @Override
    public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
        return new ArrayList<>(ClientUtil.itemTooltip(ingredient, tooltipFlag));
    }

    @Override
    public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        var renderProperty = RenderProperties.get(ingredient.getItem());
        var font = renderProperty.getFont(ingredient);
        if (font != null) {
            return font;
        }
        return minecraft.font;
    }
}
