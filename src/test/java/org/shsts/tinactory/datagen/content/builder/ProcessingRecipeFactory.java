package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipeFactory extends RecipeFactory<ProcessingRecipe.Builder,
    ProcessingRecipeFactory.Builder, ProcessingRecipeFactory> {
    public class Builder extends ProcessingRecipeBuilder<ProcessingRecipe.Builder, ProcessingRecipeFactory,
        Builder> {
        public Builder(ProcessingRecipe.Builder builder) {
            super(ProcessingRecipeFactory.this, builder);
        }
    }

    public ProcessingRecipeFactory(IRecipeType<ProcessingRecipe.Builder> recipeType) {
        super(recipeType);
    }

    @Override
    public Builder doCreateBuilder(ProcessingRecipe.Builder builder) {
        return new Builder(builder);
    }
}
