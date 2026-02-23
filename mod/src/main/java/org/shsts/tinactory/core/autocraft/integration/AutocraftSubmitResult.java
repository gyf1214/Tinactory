package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftSubmitResult(@Nullable UUID jobId, @Nullable AutocraftSubmitErrorCode errorCode) {
    public static AutocraftSubmitResult success(UUID jobId) {
        return new AutocraftSubmitResult(jobId, null);
    }

    public static AutocraftSubmitResult failure(AutocraftSubmitErrorCode errorCode) {
        return new AutocraftSubmitResult(null, errorCode);
    }

    public boolean isSuccess() {
        return jobId != null;
    }

    public Optional<UUID> optionalJobId() {
        return Optional.ofNullable(jobId);
    }
}
