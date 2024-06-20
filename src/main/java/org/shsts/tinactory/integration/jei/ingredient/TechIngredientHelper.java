package org.shsts.tinactory.integration.jei.ingredient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechIngredientHelper extends IngredientHelper<TechWrapper> {
    public TechIngredientHelper() {
        super(TechIngredientType.INSTANCE);
    }

    @Override
    public String getWildcardId(TechWrapper ingredient) {
        return "tech:" + ingredient.loc();
    }

    @Override
    public ResourceLocation getResourceLocation(TechWrapper ingredient) {
        return ingredient.loc();
    }

    @Override
    public String getDisplayName(TechWrapper ingredient) {
        return I18n.tr(ITechnology.getDescriptionId(ingredient.loc())).toString();
    }

    @Override
    public TechWrapper copyIngredient(TechWrapper ingredient) {
        return new TechWrapper(ingredient.loc());
    }
}
