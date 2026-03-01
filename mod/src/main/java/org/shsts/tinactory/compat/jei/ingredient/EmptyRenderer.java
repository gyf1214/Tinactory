package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EmptyRenderer<V> implements IIngredientRenderer<V> {
    public EmptyRenderer() {}

    @Override
    public List<Component> getTooltip(V ingredient, TooltipFlag tooltipFlag) {
        return Collections.emptyList();
    }
}
