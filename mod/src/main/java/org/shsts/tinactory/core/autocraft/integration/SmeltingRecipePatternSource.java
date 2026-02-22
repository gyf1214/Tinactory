package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SmeltingRecipePatternSource {
    public static final ResourceLocation SMELTING_RECIPE_TYPE_ID = new ResourceLocation("minecraft", "smelting");

    private final List<SmeltingRecipe> recipes;

    public SmeltingRecipePatternSource(List<SmeltingRecipe> recipes) {
        this.recipes = recipes;
    }

    public List<CraftPattern> loadPatterns() {
        var patterns = new ArrayList<CraftPattern>();
        for (var recipe : recipes) {
            var choices = recipe.getIngredients().isEmpty() ? List.<net.minecraft.world.item.ItemStack>of() :
                List.of(recipe.getIngredients().get(0).getItems());
            if (choices.isEmpty()) {
                continue;
            }
            var input = choices.stream()
                .min(Comparator.comparing($ -> {
                    var id = ForgeRegistries.ITEMS.getKey($.getItem());
                    return id == null ? "" : id.toString();
                }))
                .orElseThrow();
            var output = recipe.getResultItem();
            if (output.isEmpty()) {
                continue;
            }
            patterns.add(new CraftPattern(
                recipe.getId().toString(),
                List.of(new CraftAmount(LogisticsInventoryView.fromItemStack(input), input.getCount())),
                List.of(new CraftAmount(LogisticsInventoryView.fromItemStack(output), output.getCount())),
                new MachineRequirement(SMELTING_RECIPE_TYPE_ID, 0, List.of())));
        }
        patterns.sort(Comparator.comparing(CraftPattern::patternId));
        return patterns;
    }
}
