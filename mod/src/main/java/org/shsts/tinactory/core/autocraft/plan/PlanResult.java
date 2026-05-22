package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanResult(
    @Nullable CraftPlan plan,
    @Nullable PlanError error,
    PlanSummary summary) {
    public PlanResult {
        if ((plan == null) == (error == null)) {
            throw new IllegalArgumentException("Exactly one of plan or error must be non-null");
        }
    }

    public static PlanResult completed(CraftPlan plan) {
        return completed(plan, PlanSummary.empty());
    }

    public static PlanResult completed(CraftPlan plan, PlanSummary summary) {
        return new PlanResult(plan, null, summary);
    }

    public static PlanResult failed(PlanError error, PlanSummary summary) {
        return new PlanResult(null, error, summary);
    }
}
