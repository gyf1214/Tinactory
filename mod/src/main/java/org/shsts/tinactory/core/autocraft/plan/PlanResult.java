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
        return new PlanResult(plan, null, plan.summary());
    }

    public static PlanResult completed(CraftPlan plan, PlanSummary summary) {
        return completed(new CraftPlan(plan.steps(), summary, plan.memoryUsage()));
    }

    public static PlanResult failed(PlanError error, PlanSummary summary) {
        return new PlanResult(null, error, summary);
    }
}
