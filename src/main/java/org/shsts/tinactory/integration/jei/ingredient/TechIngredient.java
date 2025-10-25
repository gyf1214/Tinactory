package org.shsts.tinactory.integration.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.util.I18n;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record TechIngredient(ResourceLocation loc) {
    public static final IIngredientType<TechIngredient> TYPE = () -> TechIngredient.class;

    public static final IngredientHelper<TechIngredient> HELPER = new IngredientHelper<>(TYPE) {
        @Override
        public String getWildcardId(TechIngredient ingredient) {
            return "tech:" + ingredient.loc;
        }

        @Override
        public ResourceLocation getResourceLocation(TechIngredient ingredient) {
            return ingredient.loc;
        }

        @Override
        public String getDisplayName(TechIngredient ingredient) {
            return I18n.tr(ITechnology.getDescriptionId(ingredient.loc)).toString();
        }

        @Override
        public TechIngredient copyIngredient(TechIngredient ingredient) {
            return new TechIngredient(ingredient.loc);
        }
    };
}
