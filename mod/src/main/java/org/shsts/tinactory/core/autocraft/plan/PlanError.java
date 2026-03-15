package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanError(Code code, @Nullable IIngredientKey targetKey, String message, List<IIngredientKey> cyclePath) {
    public PlanError {
        cyclePath = List.copyOf(cyclePath);
    }

    public static PlanError missingPattern(IIngredientKey key) {
        return new PlanError(Code.MISSING_PATTERN, key, "No pattern can produce target", List.of());
    }

    public static PlanError unsatisfiedBaseResource(IIngredientKey key) {
        return new PlanError(Code.UNSATISFIED_BASE_RESOURCE, key, "No base resource available", List.of());
    }

    public static PlanError cycleDetected(List<IIngredientKey> cyclePath) {
        return new PlanError(Code.CYCLE_DETECTED, null, "Cycle detected in craft graph", cyclePath);
    }

    public enum Code {
        MISSING_PATTERN,
        UNSATISFIED_BASE_RESOURCE,
        CYCLE_DETECTED
    }
}
