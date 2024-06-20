package org.shsts.tinactory.integration.jei.ingredient;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechIngredientType implements IIngredientType<TechWrapper> {
    private TechIngredientType() {}

    @Override
    public Class<? extends TechWrapper> getIngredientClass() {
        return TechWrapper.class;
    }

    public static final TechIngredientType INSTANCE = new TechIngredientType();
}
