package org.shsts.tinactory.core.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeDataConsumer {
    String getModId();

    void registerRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe);

    IRecipeDataConsumer EMPTY = new IRecipeDataConsumer() {
        @Override
        public String getModId() {
            return "";
        }

        @Override
        public void registerRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe) {}
    };
}
