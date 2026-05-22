package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftExecuteResult(
    boolean successful,
    @Nullable Code errorCode) {

    public static AutocraftExecuteResult success() {
        return new AutocraftExecuteResult(true, null);
    }

    public static AutocraftExecuteResult failure(Code errorCode) {
        return new AutocraftExecuteResult(false, errorCode);
    }

    public boolean isSuccess() {
        return successful;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum Code {
        PLAN_NOT_FOUND,
        CPU_OFFLINE,
        CPU_BUSY
    }
}
