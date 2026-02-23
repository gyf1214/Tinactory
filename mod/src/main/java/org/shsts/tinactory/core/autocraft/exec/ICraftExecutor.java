package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftExecutor {
    void start(CraftPlan plan);

    void runCycle(long transmissionBandwidth);

    void cancel();

    ExecutionState state();

    @Nullable
    ExecutionError error();

    ExecutionDetails details();
}
