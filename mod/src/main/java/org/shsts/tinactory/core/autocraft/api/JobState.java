package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum JobState {
    IDLE,
    RUNNING,
    BLOCKED,
    FAILED;

    public final String id;

    JobState() {
        this.id = name().toLowerCase(Locale.ROOT);
    }

    public boolean busy() {
        return this == RUNNING || this == BLOCKED;
    }
}
