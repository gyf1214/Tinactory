package org.shsts.tinactory.core.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@FunctionalInterface
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeDataConsumer {
    void addRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe);
}
