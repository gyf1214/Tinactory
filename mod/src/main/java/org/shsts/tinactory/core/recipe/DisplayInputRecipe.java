package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DisplayInputRecipe extends ProcessingRecipe {
    protected DisplayInputRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    public DisplayInputRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power) {
        super(inputs, outputs, workTicks, voltage, power);
    }

    public static MapCodec<DisplayInputRecipe> codec(Codec<IProcessingIngredient> ingredientCodec,
        Codec<IProcessingResult> resultCodec) {
        return ProcessingRecipe.codec(ingredientCodec, resultCodec, DisplayInputRecipe::new);
    }

    public static Builder builder(IRecipeType<?> parent, ResourceLocation loc) {
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
