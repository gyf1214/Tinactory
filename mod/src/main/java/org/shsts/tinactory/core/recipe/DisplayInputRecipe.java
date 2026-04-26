package org.shsts.tinactory.core.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DisplayInputRecipe extends ProcessingRecipe {
    protected DisplayInputRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    public static Builder builder(IRecipeType<Builder> parent, ResourceLocation loc) {
        return new Builder(parent, loc) {
            @Override
            protected ProcessingRecipe createObject() {
                return new DisplayInputRecipe(this);
            }
        };
    }

    @Override
    protected Optional<IProcessingObject> getDisplayObject() {
        return inputs.isEmpty() ? Optional.empty() : Optional.of(inputs.get(0).ingredient());
    }
}
