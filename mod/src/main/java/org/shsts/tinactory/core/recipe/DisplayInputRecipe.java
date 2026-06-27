package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DisplayInputRecipe extends ProcessingRecipe {
    public DisplayInputRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power) {
        super(inputs, outputs, workTicks, voltage, power);
    }

    public static MapCodec<DisplayInputRecipe> codec(Codec<IProcessingIngredient> ingredientCodec,
        Codec<IProcessingResult> resultCodec) {
        return ProcessingRecipe.codec(ingredientCodec, resultCodec, DisplayInputRecipe::new);
    }

    @Override
    protected Optional<IProcessingObject> getDisplayObject() {
        return inputs.isEmpty() ? Optional.empty() : Optional.of(inputs.get(0).ingredient());
    }
}
