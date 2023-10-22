package org.shsts.tinactory.content;

import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.recipe.NullRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

public final class AllRecipes {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RecipeTypeEntry<NullRecipe, NullRecipe.Builder> NULL_RECIPE_TYPE;

    static {
        NULL_RECIPE_TYPE = REGISTRATE.recipeType("null", NullRecipe::serializer)
                .builder(NullRecipe.Builder::new)
                .existingType(() -> RecipeType.CRAFTING)
                .register();

        NULL_RECIPE_TYPE.recipe("oak_wood").build();
    }

    public static void init() {}
}
