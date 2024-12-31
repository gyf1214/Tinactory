package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DisplayInputRecipe extends ProcessingRecipe {
    protected DisplayInputRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    @Override
    public IProcessingObject getDisplay() {
        return inputs.get(0).ingredient();
    }

    public static Builder builder(IRecipeType<Builder> parent, ResourceLocation loc) {
        return new Builder(parent, loc) {
            @Override
            protected ProcessingRecipe createObject() {
                return new DisplayInputRecipe(this);
            }
        };
    }
}
