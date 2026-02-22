package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum ExecutionState {
    IDLE,
    RUNNING,
    BLOCKED,
    COMPLETED,
    CANCELLED,
    FAILED
}
