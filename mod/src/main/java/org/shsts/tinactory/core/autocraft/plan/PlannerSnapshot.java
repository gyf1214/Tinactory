package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.PlanningState;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlannerSnapshot(PlanningState state, @Nullable CraftPlan plan, @Nullable PlanError error) {
    public static PlannerSnapshot running() {
        return new PlannerSnapshot(PlanningState.RUNNING, null, null);
    }

    public static PlannerSnapshot completed(CraftPlan plan) {
        return new PlannerSnapshot(PlanningState.COMPLETED, plan, null);
    }

    public static PlannerSnapshot failed(PlanError error) {
        return new PlannerSnapshot(PlanningState.FAILED, null, error);
    }
}
