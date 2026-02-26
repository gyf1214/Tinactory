package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftPreviewResult(
    @Nullable UUID planId,
    @Nullable CraftPlan planSnapshot,
    List<CraftAmount> summaryOutputs,
    @Nullable AutocraftPreviewErrorCode errorCode) {

    public static AutocraftPreviewResult success(
        UUID planId, CraftPlan planSnapshot, List<CraftAmount> summaryOutputs) {
        return new AutocraftPreviewResult(planId, planSnapshot, List.copyOf(summaryOutputs), null);
    }

    public static AutocraftPreviewResult failure(AutocraftPreviewErrorCode errorCode) {
        return new AutocraftPreviewResult(null, null, List.of(), errorCode);
    }

    public boolean isSuccess() {
        return planId != null;
    }

    public Optional<UUID> optionalPlanId() {
        return Optional.ofNullable(planId);
    }
}
