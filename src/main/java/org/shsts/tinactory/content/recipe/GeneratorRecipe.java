package org.shsts.tinactory.content.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorRecipe extends ProcessingRecipe {
    protected GeneratorRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    @Override
    public boolean canCraftInVoltage(long voltage) {
        return voltage == this.voltage;
    }

    @Override
    public IProcessingObject getDisplay() {
        return inputs.get(0).ingredient();
    }

    public static class Builder extends BuilderBase<GeneratorRecipe, Builder> {
        public Builder(Registrate registrate, RecipeTypeEntry<GeneratorRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        @Override
        protected GeneratorRecipe createObject() {
            return new GeneratorRecipe(this);
        }
    }

    public static final SmartRecipeSerializer.Factory<GeneratorRecipe, Builder> SERIALIZER =
            Serializer::new;
}
