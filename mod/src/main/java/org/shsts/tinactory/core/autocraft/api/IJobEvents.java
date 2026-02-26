package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IJobEvents {
    default void onStepStarted(CraftStep step) {}

    default void onStepCompleted(CraftStep step) {}

    default void onStepBlocked(CraftStep step, String reason) {}
}
