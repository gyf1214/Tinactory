package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzer extends ProcessingMachine<OreAnalyzerRecipe> {
    private boolean emptyRecipe = false;

    public OreAnalyzer(IRecipeType<OreAnalyzerRecipe> recipeType,
        Supplier<IRecipeManager> recipeManager, IRecipeType<MarkerRecipe> markerType) {
        super(recipeType, recipeManager, markerType);
    }

    private Optional<IEntry<OreAnalyzerRecipe>> newRecipe(List<IEntry<OreAnalyzerRecipe>> matches, IMachine machine) {
        var size = matches.size();
        if (size == 0) {
            return Optional.empty();
        }

        var random = machine.random();

        var emptyRate = 1d;
        for (var match : matches) {
            emptyRate *= 1 - match.get().rate;
        }
        emptyRecipe = random.nextDouble() <= emptyRate;

        if (emptyRecipe || size == 1) {
            return Optional.of(matches.getFirst());
        }

        var rates = matches.stream().mapToDouble(r -> r.get().rate).toArray();
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
    public Optional<IEntry<OreAnalyzerRecipe>> newRecipe(IMachine machine) {
        setFilterRecipe(machine, null);
        var matches = recipeManager().getRecipesFor(recipeType, machine);
        return newRecipe(matches, machine);
    }

    @Override
    public Optional<IEntry<OreAnalyzerRecipe>> newRecipe(IMachine machine, ResourceLocation target) {
        var marker = recipeManager().byLoc(markerType, target);
        if (marker.isPresent()) {
            setFilterRecipe(machine, marker.get());
            var recipe = marker.get().get();
            var matches = recipeManager().getRecipesFor(recipeType, machine)
                .stream().filter(recipe::matches)
                .toList();
            return newRecipe(matches, machine);
        }

        var processing = recipeManager().byLoc(recipeType, target);
        if (processing.isPresent()) {
            setFilterRecipe(machine, processing.get());
            var recipe = processing.get().get();
            if (recipe.matches(machine)) {
                var random = machine.random();
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
    public void onWorkDone(IEntry<OreAnalyzerRecipe> recipe, IMachine machine, RandomSource random,
        Consumer<IProcessingResult> callback) {
        if (!emptyRecipe) {
            super.onWorkDone(recipe, machine, random, callback);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = super.serializeNBT(provider);
        tag.putBoolean("empty", emptyRecipe);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        emptyRecipe = tag.getBoolean("empty");
    }
}
