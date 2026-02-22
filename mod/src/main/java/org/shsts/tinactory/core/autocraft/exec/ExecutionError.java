package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ExecutionError(Code code, String stepId, String message) {
    public enum Code {
        INPUT_MISSING,
        MACHINE_UNAVAILABLE,
        CANCELLED
    }
}
