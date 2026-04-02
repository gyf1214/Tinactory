package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.PlanningState;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlannerSnapshot(PlanningState state, CraftPlan plan, PlanError error) {
    public static PlannerSnapshot running() {
        return new PlannerSnapshot(PlanningState.RUNNING, null, PlanError.none());
    }

    public static PlannerSnapshot completed(CraftPlan plan) {
        return new PlannerSnapshot(PlanningState.COMPLETED, plan, PlanError.none());
    }

    public static PlannerSnapshot failed(PlanError error) {
        return new PlannerSnapshot(PlanningState.FAILED, null, error);
    }
}
