package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;

import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SmeltingRecipePatternSource {
    public static final ResourceLocation SMELTING_RECIPE_TYPE_ID = new ResourceLocation("minecraft", "smelting");

    public SmeltingRecipePatternSource() {}

    @Nullable
    public CraftPattern convertRecipe(SmeltingRecipe recipe) {
        var choices = recipe.getIngredients().isEmpty() ? List.<ItemStack>of() :
            List.of(recipe.getIngredients().get(0).getItems());
        if (choices.isEmpty()) {
            return null;
        }
        var input = choices.stream()
            .min(Comparator.comparing($ -> {
                var id = ForgeRegistries.ITEMS.getKey($.getItem());
                return id == null ? "" : id.toString();
            }))
            .orElseThrow();
        var output = recipe.getResultItem();
        if (output.isEmpty()) {
            return null;
        }
        return new CraftPattern(
            recipe.getId().toString(),
            List.of(new CraftAmount(LogisticsInventoryView.fromItemStack(input), input.getCount())),
            List.of(new CraftAmount(LogisticsInventoryView.fromItemStack(output), output.getCount())),
            new MachineRequirement(SMELTING_RECIPE_TYPE_ID, 0, List.of()));
    }
}
