package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum JobState {
    IDLE,
    RUNNING,
    BLOCKED,
    COMPLETED,
    CANCELLED,
    FAILED
}
