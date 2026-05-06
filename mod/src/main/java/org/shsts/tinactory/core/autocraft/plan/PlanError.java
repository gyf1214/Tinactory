package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanError(Code code, IStackKey targetKey) {
    public static PlanError missingPattern(IStackKey key) {
        return new PlanError(Code.MISSING_PATTERN, key);
    }

    public static PlanError unsatisfiedBaseResource(IStackKey key) {
        return new PlanError(Code.UNSATISFIED_BASE_RESOURCE, key);
    }

    public static PlanError cycleDetected(IStackKey key) {
        return new PlanError(Code.CYCLE_DETECTED, key);
    }

    public enum Code {
        MISSING_PATTERN,
        UNSATISFIED_BASE_RESOURCE,
        CYCLE_DETECTED
    }
}
