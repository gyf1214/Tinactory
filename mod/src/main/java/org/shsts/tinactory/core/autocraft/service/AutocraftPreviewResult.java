package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.PlanError;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftPreviewResult(
    @Nullable AutocraftPreview preview,
    PlanError error) {

    public static AutocraftPreviewResult empty() {
        return new AutocraftPreviewResult(null, PlanError.none());
    }

    public static AutocraftPreviewResult success(AutocraftPreview preview) {
        return new AutocraftPreviewResult(preview, PlanError.none());
    }

    public static AutocraftPreviewResult failure(PlanError error) {
        return new AutocraftPreviewResult(null, error);
    }

    public boolean isSuccess() {
        return preview != null;
    }

    public boolean isEmpty() {
        return preview == null && error.code() == PlanError.Code.NONE;
    }

    @Nullable
    public List<CraftAmount> targets() {
        return preview != null ? preview.targets() : null;
    }

    @Nullable
    public CraftPlan planSnapshot() {
        return preview != null ? preview.planSnapshot() : null;
    }
}
