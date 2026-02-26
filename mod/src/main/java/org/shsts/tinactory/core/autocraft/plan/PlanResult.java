package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanResult(@Nullable CraftPlan plan, @Nullable PlanError error) {
    public static PlanResult success(CraftPlan plan) {
        return new PlanResult(plan, null);
    }

    public static PlanResult failure(PlanError error) {
        return new PlanResult(null, error);
    }

    public boolean isSuccess() {
        return plan != null;
    }
}
