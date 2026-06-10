package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftPlan(
    List<CraftStep> steps,
    PlanSummary summary,
    long memoryUsage) {
    public CraftPlan {
        steps = List.copyOf(steps);
        memoryUsage = Math.max(0L, memoryUsage);
    }

    public CraftPlan(List<CraftStep> steps) {
        this(steps, PlanSummary.empty(), 0L);
    }
}
