package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TestRecipeManager implements IRecipeManager {
    private final Map<ResourceLocation, IRecipe<?>> byLoc = new HashMap<>();
    private final Map<ResourceLocation, List<IRecipe<?>>> byType = new HashMap<>();

    public <R extends IRecipe<?>> TestRecipeManager add(IRecipeType<?> recipeType, R recipe) {
        byLoc.put(recipe.loc(), recipe);
        byType.computeIfAbsent(recipeType.loc(), $ -> new ArrayList<>()).add(recipe);
        return this;
    }

    @Override
    public <C, R extends IRecipe<C>, B extends IRecipeBuilderBase<R>> Optional<R> getRecipeFor(
        IRecipeType<B> recipeType, C context, Level world) {
        return getRecipesFor(recipeType, context, world).stream().findFirst();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C, R extends IRecipe<C>, B extends IRecipeBuilderBase<R>> List<R> getRecipesFor(
        IRecipeType<B> recipeType, C context, Level world) {
        return getAllRecipesFor(recipeType).stream()
            .filter($ -> $.matches(context))
            .map($ -> (R) $)
            .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends IRecipe<?>, B extends IRecipeBuilderBase<R>> List<R> getAllRecipesFor(IRecipeType<B> recipeType) {
        return (List<R>) byType.getOrDefault(recipeType.loc(), List.of());
    }

    @Override
    public List<IRecipe<?>> getRawRecipesFor(IRecipeType<?> recipeType) {
        return List.copyOf(byType.getOrDefault(recipeType.loc(), List.of()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends IRecipe<?>, B extends IRecipeBuilderBase<R>> Optional<R> byLoc(
        IRecipeType<B> recipeType, ResourceLocation loc) {
        return Optional.ofNullable(byLoc.get(loc))
            .filter($ -> byType.getOrDefault(recipeType.loc(), List.of()).contains($))
            .map($ -> (R) $);
    }

    @Override
    public Optional<IRecipe<?>> byLoc(ResourceLocation loc) {
        return Optional.ofNullable(byLoc.get(loc));
    }
}
