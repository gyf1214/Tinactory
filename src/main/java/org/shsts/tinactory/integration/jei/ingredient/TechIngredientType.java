package org.shsts.tinactory.integration.jei.ingredient;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;

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
