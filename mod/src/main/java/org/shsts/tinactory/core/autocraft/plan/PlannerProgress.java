package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlannerProgress(State state, @Nullable PlanResult result) {
    public static PlannerProgress running() {
        return new PlannerProgress(State.RUNNING, null);
    }

    public static PlannerProgress done(PlanResult result) {
        return new PlannerProgress(State.DONE, result);
    }

    public static PlannerProgress failed(PlanResult result) {
        return new PlannerProgress(State.FAILED, result);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum State {
        RUNNING,
        DONE,
        FAILED
    }
}
