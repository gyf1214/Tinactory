package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.PlanningState;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlannerSnapshot(
    PlanningState state,
    @Nullable CraftPlan plan,
    @Nullable PlanError error,
    PlanSummary summary) {
    public static PlannerSnapshot running() {
        return new PlannerSnapshot(PlanningState.RUNNING, null, null, PlanSummary.empty());
    }

    public static PlannerSnapshot completed(CraftPlan plan) {
        return completed(plan, PlanSummary.empty());
    }

    public static PlannerSnapshot completed(CraftPlan plan, PlanSummary summary) {
        return new PlannerSnapshot(PlanningState.COMPLETED, plan, null, summary);
    }

    public static PlannerSnapshot failed(PlanError error, PlanSummary summary) {
        return new PlannerSnapshot(PlanningState.FAILED, null, error, summary);
    }
}
