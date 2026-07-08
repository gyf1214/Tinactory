package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.unit.fixture.TestCodecHelper.createEntry;

public final class TestRecipeManager implements IRecipeManager {
    private final Map<ResourceLocation, IEntry<? extends IRecipe<?>>> byLoc = new HashMap<>();
    private final Map<ResourceLocation, List<IEntry<? extends IRecipe<?>>>> byType = new HashMap<>();

    public <R extends IRecipe<?>> TestRecipeManager add(IRecipeType<R> recipeType, IEntry<R> recipe) {
        return add(recipeType, recipe.loc(), recipe.get());
    }

    public <R extends IRecipe<?>> TestRecipeManager add(IRecipeType<R> recipeType, ResourceLocation loc, R recipe) {
        var entry = createEntry(loc, recipe);
        byLoc.put(loc, entry);
        byType.computeIfAbsent(recipeType.loc(), $ -> new ArrayList<>()).add(entry);
        return this;
    }

    @Override
    public <C, R extends IRecipe<C>> Optional<IEntry<R>> getRecipeFor(IRecipeType<R> recipeType, C context) {
        return getRecipesFor(recipeType, context).stream().findFirst();
    }

    @Override
    public <C, R extends IRecipe<C>> List<IEntry<R>> getRecipesFor(IRecipeType<R> recipeType, C context) {
        return getAllRecipesFor(recipeType).stream()
            .filter($ -> $.get().matches(context))
            .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends IRecipe<?>> List<IEntry<R>> getAllRecipesFor(IRecipeType<R> recipeType) {
        return (List<IEntry<R>>) (List<?>) byType.getOrDefault(recipeType.loc(), List.of());
    }

    @Override
    public List<IEntry<? extends IRecipe<?>>> getRawRecipesFor(IRecipeType<?> recipeType) {
        return List.copyOf(byType.getOrDefault(recipeType.loc(), List.of()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends IRecipe<?>> Optional<IEntry<R>> byLoc(IRecipeType<R> recipeType, ResourceLocation loc) {
        return Optional.ofNullable(byLoc.get(loc))
            .filter($ -> byType.getOrDefault(recipeType.loc(), List.of()).contains($))
            .map($ -> (IEntry<R>) $);
    }

    @Override
    public Optional<IEntry<? extends IRecipe<?>>> byLoc(ResourceLocation loc) {
        return Optional.ofNullable(byLoc.get(loc));
    }
}
