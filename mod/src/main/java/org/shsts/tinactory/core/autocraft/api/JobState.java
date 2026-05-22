package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum JobState {
    IDLE,
    RUNNING,
    BLOCKED,
    FAILED;

    public final String id;

    JobState() {
        this.id = name().toLowerCase();
    }

    public boolean busy() {
        return this == RUNNING || this == BLOCKED;
    }
}
