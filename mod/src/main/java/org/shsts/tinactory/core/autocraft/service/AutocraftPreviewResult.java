package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftPreviewResult(
    @Nullable AutocraftPreview preview,
    @Nullable Code errorCode) {

    public static AutocraftPreviewResult success(AutocraftPreview preview) {
        return new AutocraftPreviewResult(preview, null);
    }

    public static AutocraftPreviewResult failure(Code errorCode) {
        return new AutocraftPreviewResult(null, errorCode);
    }

    public boolean isSuccess() {
        return preview != null;
    }

    @Nullable
    public List<CraftAmount> targets() {
        return preview != null ? preview.targets() : null;
    }

    @Nullable
    public CraftPlan planSnapshot() {
        return preview != null ? preview.planSnapshot() : null;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum Code {
        INVALID_REQUEST,
        PLAN_FAILED
    }
}
