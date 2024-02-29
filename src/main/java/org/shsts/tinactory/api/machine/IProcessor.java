package org.shsts.tinactory.api.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IProcessor {
    /**
     * Must be called from Server.
     */
    void onPreWork();

    /**
     * Must be called from Server.
     */
    void onWorkTick(double partial);

    Optional<ProcessingRecipe<?>> getCurrentRecipe();

    double getProgress();

    void onInputUpdate();

    void onOutputUpdate();
}
