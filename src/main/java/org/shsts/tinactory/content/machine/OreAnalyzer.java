package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllRecipes.MARKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzer extends ProcessingMachine<OreAnalyzerRecipe> {
    private boolean emptyRecipe = false;

    public OreAnalyzer(IRecipeType<OreAnalyzerRecipe.Builder> recipeType) {
        super(recipeType);
    }

    @Override
    protected List<ProcessingRecipe> targetRecipes(Level world, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        return markers(recipeManager, machine)
            .map($ -> (ProcessingRecipe) $)
            .toList();
    }

    @Override
    public boolean allowTargetRecipe(Level world, ResourceLocation loc, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        var marker = recipeManager.byLoc(MARKER, loc);
        if (marker.isEmpty()) {
            return false;
        }
        var recipe = marker.get();
        return recipe.matches(recipeType) && recipe.canCraft(machine);
    }

    private Optional<OreAnalyzerRecipe> newRecipe(List<OreAnalyzerRecipe> matches, Level world) {
        var size = matches.size();
        if (size == 0) {
            return Optional.empty();
        }

        var random = world.random;

        var emptyRate = 1d;
        for (var match : matches) {
            emptyRate *= 1 - match.rate;
        }
        emptyRecipe = random.nextDouble() <= emptyRate;

        if (emptyRecipe || size == 1) {
            return Optional.of(matches.get(0));
        }

        var rates = matches.stream().mapToDouble(r -> r.rate).toArray();
        for (var i = 1; i < size; i++) {
            rates[i] += rates[i - 1];
        }
        for (var i = 0; i < size; i++) {
            rates[i] /= rates[size - 1];
        }
        var rn = random.nextDouble();
        for (var i = 0; i < size; i++) {
            if (rn <= rates[i]) {
                return Optional.of(matches.get(i));
            }
        }
        return Optional.of(matches.get(size - 1));
    }

    @Override
    public Optional<OreAnalyzerRecipe> newRecipe(Level world, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        var matches = recipeManager.getRecipesFor(recipeType, machine, world);
        return newRecipe(matches, world);
    }

    @Override
    public Optional<OreAnalyzerRecipe> newRecipe(Level world, IMachine machine, ResourceLocation target) {
        var recipeManager = CORE.recipeManager(world);
        var marker = recipeManager.byLoc(MARKER, target);
        if (marker.isEmpty()) {
            return Optional.empty();
        }
        var matches = recipeManager.getRecipesFor(recipeType, machine, world)
            .stream().filter(marker.get()::matches)
            .toList();
        return newRecipe(matches, world);
    }

    @Override
    public void onWorkDone(OreAnalyzerRecipe recipe, IMachine machine, Random random) {
        if (!emptyRecipe) {
            machine.container().ifPresent(container -> recipe.doInsertOutputs(container, random));
        }
    }
}
