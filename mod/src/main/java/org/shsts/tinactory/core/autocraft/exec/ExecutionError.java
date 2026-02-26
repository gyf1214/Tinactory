package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ExecutionError(Code code, String stepId, String message) {
    public enum Code {
        INPUT_UNAVAILABLE,
        MACHINE_UNAVAILABLE,
        MACHINE_REASSIGNMENT_BLOCKED,
        FLUSH_BACKPRESSURE,
        CANCELLED
    }
}
