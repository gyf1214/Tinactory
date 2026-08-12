package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachineProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IBoiler extends IMachineProcessor {
    /**
     * This is only for display purposes.
     */
    double minHeat();

    /**
     * This is only for display purposes.
     */
    double maxHeat();

    double heat();

    default double heatProgress() {
        return Math.clamp((heat() - minHeat()) / (maxHeat() - minHeat()), 0d, 1d);
    }

    @Override
    default boolean supportsRecipeType(ResourceLocation recipeTypeId) {
        return false;
    }
}
