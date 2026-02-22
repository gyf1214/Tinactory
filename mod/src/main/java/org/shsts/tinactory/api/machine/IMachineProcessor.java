package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineProcessor extends IProcessor {
    Optional<IProcessingObject> getInfo(int port, int index);

    List<IProcessingObject> getAllInfo();

    long progressTicks();

    long maxProgressTicks();

    double workSpeed();

    default boolean supportsRecipeType(ResourceLocation recipeTypeId) {
        return false;
    }

    @Override
    default double getProgress() {
        var maxProgress = maxProgressTicks();
        if (maxProgress <= 0) {
            return progressTicks() > 0 ? 1d : 0d;
        }
        return (double) progressTicks() / (double) maxProgress;
    }
}
