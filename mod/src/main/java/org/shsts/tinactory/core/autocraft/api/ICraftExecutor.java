package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ExecutorRuntimeSnapshot;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftExecutor {
    void start(CraftPlan plan);

    void restore(CraftPlan plan, ExecutorRuntimeSnapshot snapshot);

    void runCycle(long transmissionBandwidth);

    void cancel();

    ExecutionState state();

    @Nullable
    ExecutionError error();

    ExecutionDetails details();

    CraftPlan currentPlan();

    int nextStepIndex();

    ExecutorRuntimeSnapshot snapshot();
}
