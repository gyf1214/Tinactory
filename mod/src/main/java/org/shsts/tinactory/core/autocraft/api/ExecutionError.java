package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import static org.shsts.tinactory.core.util.LocHelper.constantToId;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum ExecutionError {
    NONE,
    OFFLINE,
    INPUT_UNAVAILABLE,
    MACHINE_UNAVAILABLE,
    MACHINE_REASSIGNMENT_BLOCKED,
    FLUSH_BACKPRESSURE;

    public final String id;

    ExecutionError() {
        this.id = constantToId(name());
    }
}
