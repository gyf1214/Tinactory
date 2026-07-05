package org.shsts.tinactory.core.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DisplayInputRecipe extends ProcessingRecipe {
    public DisplayInputRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power) {
        super(inputs, outputs, workTicks, voltage, power);
    }

    @Override
    protected Optional<IProcessingObject> getDisplayObject() {
        return inputs.isEmpty() ? Optional.empty() : Optional.of(inputs.getFirst().ingredient());
    }
}
