package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorRecipe extends ProcessingRecipe {
    protected GeneratorRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    @Override
    public boolean matches(IContainer container, Level world) {
        // no check output
        return canCraftIn(container) &&
            inputs.stream().allMatch(input -> consumeInput(container, input, true));
    }

    @Override
    public boolean canCraftInVoltage(long voltage) {
        return voltage == this.voltage;
    }

    @Override
    public IProcessingObject getDisplay() {
        return inputs.get(0).ingredient();
    }

    public static ProcessingRecipe.Builder builder(IRecipeDataConsumer consumer,
        RecipeTypeEntry<ProcessingRecipe, Builder> parent, ResourceLocation loc) {
        return new Builder(consumer, parent, loc) {
            @Override
            protected ProcessingRecipe createObject() {
                return new GeneratorRecipe(this);
            }
        };
    }
}
