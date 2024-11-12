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

import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;
import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RatedItemIngredientRenderer implements IIngredientRenderer<ItemStack> {
    private final double rate;

    public RatedItemIngredientRenderer(double rate) {
        this.rate = rate;
    }

    @Override
    public void render(PoseStack poseStack, ItemStack ingredient) {
        var poseStack1 = RenderUtil.applyToModelViewStack(poseStack);
        RenderUtil.renderFakeItemWithDecoration(ingredient, 0, 0, null);
        RenderUtil.popModelViewStack(poseStack1);
    }

    @Override
    public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
        var lines = new ArrayList<>(ClientUtil.itemTooltip(ingredient, tooltipFlag));
        lines.add(formatRate(rate));
        return lines;
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

    public static Component formatRate(double rate) {
        return tr("tinactory.jei.rate", PERCENTAGE_FORMAT.format(rate));
    }
}
