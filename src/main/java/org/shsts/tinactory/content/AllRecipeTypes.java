package org.shsts.tinactory.content;

import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.recipe.NullRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

public final class AllRecipeTypes {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RecipeTypeEntry<NullRecipe> NULL_RECIPE;

    static {
        NULL_RECIPE = REGISTRATE.recipeType("null", NullRecipe::serializer)
                .existingType(() -> RecipeType.CRAFTING)
                .register();
    }

    public static void init() {}
}
