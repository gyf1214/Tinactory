package org.shsts.tinactory.integration.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.MethodsReturnNonnullByDefault;

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
