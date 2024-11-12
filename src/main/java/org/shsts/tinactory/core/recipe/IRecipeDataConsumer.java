package org.shsts.tinactory.core.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.SmartRecipe;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeDataConsumer {
    String getModId();

    void registerRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe);

    default void registerSmartRecipe(ResourceLocation loc, Supplier<SmartRecipe<?>> recipe) {
        registerRecipe(loc, () -> recipe.get().toFinished());
    }

    IRecipeDataConsumer EMPTY = new IRecipeDataConsumer() {
        @Override
        public String getModId() {
            return "";
        }

        @Override
        public void registerRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe) {}
    };
}
