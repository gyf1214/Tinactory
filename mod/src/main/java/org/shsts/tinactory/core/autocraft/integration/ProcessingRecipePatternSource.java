package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ProcessingRecipePatternSource {
    private final ResourceLocation recipeTypeId;
    private final List<? extends ProcessingRecipe> recipes;

    public ProcessingRecipePatternSource(ResourceLocation recipeTypeId, List<? extends ProcessingRecipe> recipes) {
        this.recipeTypeId = recipeTypeId;
        this.recipes = recipes;
    }

    public List<CraftPattern> loadPatterns() {
        var patterns = new ArrayList<CraftPattern>();
        for (var recipe : recipes) {
            var inputs = new ArrayList<CraftAmount>();
            var outputs = new ArrayList<CraftAmount>();
            var ok = true;
            for (var input : recipe.inputs) {
                var converted = convertIngredient(input.ingredient());
                if (converted == null) {
                    ok = false;
                    break;
                }
                inputs.add(converted);
            }
            if (!ok) {
                continue;
            }
            for (var output : recipe.outputs) {
                var converted = convertResult(output.result());
                if (converted == null) {
                    ok = false;
                    break;
                }
                outputs.add(converted);
            }
            if (!ok || outputs.isEmpty()) {
                continue;
            }
            patterns.add(new CraftPattern(
                recipe.loc().toString(),
                inputs,
                outputs,
                new MachineRequirement(recipeTypeId, 0, List.of())));
        }
        patterns.sort(Comparator.comparing(CraftPattern::patternId));
        return patterns;
    }

    @Nullable
    private static CraftAmount convertIngredient(IProcessingIngredient ingredient) {
        if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            return new CraftAmount(LogisticsInventoryView.fromItemStack(item.stack()), item.stack().getCount());
        }
        if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase items) {
            var choices = items.ingredient.getItems();
            if (choices.length <= 0) {
                return null;
            }
            var selected = List.of(choices).stream()
                .min(Comparator.comparing($ -> {
                    var id = ForgeRegistries.ITEMS.getKey($.getItem());
                    return id == null ? "" : id.toString();
                }));
            if (selected.isEmpty()) {
                return null;
            }
            var size = Math.max(1, items.amount);
            return new CraftAmount(LogisticsInventoryView.fromItemStack(selected.get()), size);
        }
        if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
            return new CraftAmount(LogisticsInventoryView.fromFluidStack(fluid.fluid()), fluid.fluid().getAmount());
        }
        return null;
    }

    @Nullable
    private static CraftAmount convertResult(IProcessingResult result) {
        if (result instanceof ProcessingResults.ItemResult item) {
            if (item.rate < 1d) {
                return null;
            }
            return new CraftAmount(LogisticsInventoryView.fromItemStack(item.stack), item.stack.getCount());
        }
        if (result instanceof ProcessingResults.FluidResult fluid) {
            if (fluid.rate < 1d) {
                return null;
            }
            return new CraftAmount(LogisticsInventoryView.fromFluidStack(fluid.stack), fluid.stack.getAmount());
        }
        return null;
    }
}
