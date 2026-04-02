package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum ExecutionError {
    NONE,
    INPUT_UNAVAILABLE,
    MACHINE_UNAVAILABLE,
    MACHINE_REASSIGNMENT_BLOCKED,
    FLUSH_BACKPRESSURE
}
