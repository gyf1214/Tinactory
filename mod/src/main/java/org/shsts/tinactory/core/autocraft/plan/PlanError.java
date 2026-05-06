package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanError(Code code, @Nullable IStackKey targetKey, List<IStackKey> cyclePath) {
    public PlanError {
        cyclePath = List.copyOf(cyclePath);
    }

    public static PlanError missingPattern(IStackKey key) {
        return new PlanError(Code.MISSING_PATTERN, key, List.of());
    }

    public static PlanError unsatisfiedBaseResource(IStackKey key) {
        return new PlanError(Code.UNSATISFIED_BASE_RESOURCE, key, List.of());
    }

    public static PlanError cycleDetected(List<IStackKey> cyclePath) {
        return new PlanError(Code.CYCLE_DETECTED, null, cyclePath);
    }

    public enum Code {
        MISSING_PATTERN,
        UNSATISFIED_BASE_RESOURCE,
        CYCLE_DETECTED
    }
}
