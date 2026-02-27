package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanError(Code code, @Nullable CraftKey targetKey, String message, List<CraftKey> cyclePath) {
    public PlanError {
        cyclePath = List.copyOf(cyclePath);
    }

    public static PlanError missingPattern(CraftKey key) {
        return new PlanError(Code.MISSING_PATTERN, key, "No pattern can produce target", List.of());
    }

    public static PlanError unsatisfiedBaseResource(CraftKey key) {
        return new PlanError(Code.UNSATISFIED_BASE_RESOURCE, key, "No base resource available", List.of());
    }

    public static PlanError cycleDetected(List<CraftKey> cyclePath) {
        return new PlanError(Code.CYCLE_DETECTED, null, "Cycle detected in craft graph", cyclePath);
    }

    public enum Code {
        MISSING_PATTERN,
        UNSATISFIED_BASE_RESOURCE,
        CYCLE_DETECTED
    }
}
