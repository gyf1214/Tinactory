package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

/**
 * The ingredient to mark a specific recipe for recipe book.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record RecipeMarker(ResourceLocation loc) {
    public static final IIngredientType<RecipeMarker> TYPE = () -> RecipeMarker.class;

    public static final IngredientHelper<RecipeMarker> HELPER = new IngredientHelper<>(TYPE) {
        @Override
        public String getWildcardId(RecipeMarker ingredient) {
            return "recipe:" + ingredient.loc;
        }

        @Override
        public String getDisplayName(RecipeMarker ingredient) {
            return "";
        }

        @Override
        public ResourceLocation getResourceLocation(RecipeMarker ingredient) {
            return ingredient.loc;
        }

        @Override
        public RecipeMarker copyIngredient(RecipeMarker ingredient) {
            return new RecipeMarker(ingredient.loc);
        }
    };
}
