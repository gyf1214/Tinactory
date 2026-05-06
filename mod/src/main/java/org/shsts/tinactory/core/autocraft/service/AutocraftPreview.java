package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftPreview(
    @Nullable CraftPlan planSnapshot,
    @Nullable PlanError error,
    PlanSummary summary) {

    public static AutocraftPreview empty() {
        return new AutocraftPreview(null, null, PlanSummary.empty());
    }

    public static AutocraftPreview success(CraftPlan planSnapshot, PlanSummary summary) {
        return new AutocraftPreview(planSnapshot, null, summary);
    }

    public static AutocraftPreview failure(PlanError error, PlanSummary summary) {
        return new AutocraftPreview(null, error, summary);
    }

    public boolean isSuccess() {
        return planSnapshot != null;
    }

    public boolean isEmpty() {
        return planSnapshot == null && error == null;
    }
}
