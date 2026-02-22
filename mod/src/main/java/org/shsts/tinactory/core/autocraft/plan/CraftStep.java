package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftStep(String stepId, CraftPattern pattern, long runs) {
    public CraftStep {
        if (stepId.isBlank()) {
            throw new IllegalArgumentException("stepId must not be blank");
        }
        if (runs <= 0L) {
            throw new IllegalArgumentException("runs must be positive");
        }
    }
}
