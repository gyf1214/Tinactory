package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftExecuteResult(
    @Nullable UUID jobId,
    @Nullable Code errorCode) {

    public static AutocraftExecuteResult success(UUID jobId) {
        return new AutocraftExecuteResult(jobId, null);
    }

    public static AutocraftExecuteResult failure(Code errorCode) {
        return new AutocraftExecuteResult(null, errorCode);
    }

    public boolean isSuccess() {
        return jobId != null;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum Code {
        PLAN_NOT_FOUND,
        CPU_OFFLINE,
        CPU_BUSY
    }
}
