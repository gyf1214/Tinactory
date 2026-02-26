package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftExecuteResult(
    @Nullable UUID jobId,
    @Nullable AutocraftExecuteErrorCode errorCode,
    Map<CraftKey, Long> missingInputs) {

    public static AutocraftExecuteResult success(UUID jobId) {
        return new AutocraftExecuteResult(jobId, null, Map.of());
    }

    public static AutocraftExecuteResult failure(AutocraftExecuteErrorCode errorCode) {
        return new AutocraftExecuteResult(null, errorCode, Map.of());
    }

    public static AutocraftExecuteResult failure(
        AutocraftExecuteErrorCode errorCode, Map<CraftKey, Long> missingInputs) {
        return new AutocraftExecuteResult(null, errorCode, Map.copyOf(missingInputs));
    }

    public boolean isSuccess() {
        return jobId != null;
    }

    public Optional<UUID> optionalJobId() {
        return Optional.ofNullable(jobId);
    }
}
