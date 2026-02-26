package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum AutocraftExecuteErrorCode {
    INVALID_REQUEST,
    PLAN_NOT_FOUND,
    CPU_NOT_VISIBLE,
    CPU_OFFLINE,
    CPU_BUSY,
    PREFLIGHT_MISSING_INPUTS,
    EXECUTOR_START_FAILED
}
