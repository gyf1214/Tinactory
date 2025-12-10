package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.machine.ProcessingInfo;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllRecipes.MARKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzer extends ProcessingMachine<OreAnalyzerRecipe> {
    private boolean emptyRecipe = false;

    public OreAnalyzer(IRecipeType<OreAnalyzerRecipe.Builder> recipeType) {
        super(recipeType);
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
        setFilterRecipe(machine, null);
        var matches = recipeManager.getRecipesFor(recipeType, machine, world);
        return newRecipe(matches, world);
    }

    @Override
    public Optional<OreAnalyzerRecipe> newRecipe(Level world, IMachine machine, ResourceLocation target) {
        var recipeManager = CORE.recipeManager(world);
        var marker = recipeManager.byLoc(MARKER, target);
        if (marker.isPresent()) {
            var recipe = marker.get();
            setFilterRecipe(machine, recipe);
            var matches = recipeManager.getRecipesFor(recipeType, machine, world)
                .stream().filter(recipe::matches)
                .toList();
            return newRecipe(matches, world);
        }

        var processing = recipeManager.byLoc(recipeType, target);
        if (processing.isPresent()) {
            var recipe = processing.get();
            setFilterRecipe(machine, recipe);
            if (recipe.matches(machine, world)) {
                var random = world.random;
                emptyRecipe = random.nextDouble() > recipe.rate;
                return processing;
            }
        }

        return Optional.empty();
    }

    @Override
    protected void addOutputInfo(OreAnalyzerRecipe recipe, int parallel, Consumer<ProcessingInfo> info) {
        if (!emptyRecipe) {
            super.addOutputInfo(recipe, parallel, info);
        }
    }

    @Override
    public void onWorkDone(OreAnalyzerRecipe recipe, IMachine machine, Random random,
        Consumer<IProcessingResult> callback) {
        if (!emptyRecipe) {
            super.onWorkDone(recipe, machine, random, callback);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putBoolean("empty", emptyRecipe);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        emptyRecipe = tag.getBoolean("empty");
    }
}
