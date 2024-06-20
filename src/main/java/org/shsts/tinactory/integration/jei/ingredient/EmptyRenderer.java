package org.shsts.tinactory.integration.jei.ingredient;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EmptyRenderer<V> implements IIngredientRenderer<V> {
    private EmptyRenderer() {}

    @Override
    public List<Component> getTooltip(V ingredient, TooltipFlag tooltipFlag) {
        return List.of();
    }

    public static <V> IIngredientRenderer<V> instance() {
        return new EmptyRenderer<>();
    }
}
