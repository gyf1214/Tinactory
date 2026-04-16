package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinycorelib.api.core.DistLazy;

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

    boolean supportsRecipeType(ResourceLocation recipeTypeId);

    DistLazy<List<IRecipeBookItem>> recipeBookItems();

    @Override
    default double getProgress() {
        var maxProgress = maxProgressTicks();
        if (maxProgress <= 0) {
            return progressTicks() > 0 ? 1d : 0d;
        }
        return (double) progressTicks() / (double) maxProgress;
    }
}
