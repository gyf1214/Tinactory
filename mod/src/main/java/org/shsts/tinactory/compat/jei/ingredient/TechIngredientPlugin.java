package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;

/**
 * This plugin looks up tech dependencies at runtime.
 * It is needed because when JEI builds ingredients,
 * the client may not receive TECH_INIT packet yet.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechIngredientPlugin implements ISimpleRecipeManagerPlugin<IEntry<ResearchRecipe>> {
    private final TechIngredientIndex index;
    private final IRecipeType<?> recipeType;

    public TechIngredientPlugin(TechIngredientIndex index, IRecipeType<?> recipeType) {
        this.index = index;
        this.recipeType = recipeType;
    }

    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return input.getType() == TechIngredient.TYPE;
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<IEntry<ResearchRecipe>> getRecipesForInput(ITypedIngredient<?> input) {
        if (!(input.getIngredient() instanceof TechIngredient(ResourceLocation loc))) {
            return List.of();
        }
        return index.getRecipesByRequiredTech(loc, recipeType).stream()
            .map($ -> (IEntry<ResearchRecipe>) $)
            .toList();
    }

    @Override
    public List<IEntry<ResearchRecipe>> getRecipesForOutput(ITypedIngredient<?> output) {
        return List.of();
    }

    @Override
    public List<IEntry<ResearchRecipe>> getAllRecipes() {
        return List.of();
    }
}
