package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanError(Code code, @Nullable IIngredientKey targetKey, List<IIngredientKey> cyclePath) {
    public PlanError {
        cyclePath = List.copyOf(cyclePath);
    }

    public static PlanError none() {
        return new PlanError(Code.NONE, null, List.of());
    }

    public static PlanError missingPattern(IIngredientKey key) {
        return new PlanError(Code.MISSING_PATTERN, key, List.of());
    }

    public static PlanError unsatisfiedBaseResource(IIngredientKey key) {
        return new PlanError(Code.UNSATISFIED_BASE_RESOURCE, key, List.of());
    }

    public static PlanError cycleDetected(List<IIngredientKey> cyclePath) {
        return new PlanError(Code.CYCLE_DETECTED, null, cyclePath);
    }

    public enum Code {
        NONE,
        MISSING_PATTERN,
        UNSATISFIED_BASE_RESOURCE,
        CYCLE_DETECTED
    }
}
