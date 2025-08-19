package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipeFactory1 extends RecipeFactory1<ProcessingRecipe.Builder,
    ProcessingRecipeFactory1.Builder, ProcessingRecipeFactory1> {
    public class Builder extends ProcessingRecipeBuilder1<ProcessingRecipe.Builder, ProcessingRecipeFactory1,
        Builder> {
        public Builder(ProcessingRecipe.Builder builder) {
            super(ProcessingRecipeFactory1.this, builder);
        }
    }

    public ProcessingRecipeFactory1(IRecipeType<ProcessingRecipe.Builder> recipeType) {
        super(recipeType);
    }

    @Override
    public Builder doCreateBuilder(ProcessingRecipe.Builder builder) {
        return new Builder(builder);
    }
}
