package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.exec.ExecutorSnapshot;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftExecutor {
    void start(CraftPlan plan);

    void restore(ExecutorSnapshot snapshot);

    void runCycle(long transmissionBandwidth);

    void cancel();

    ExecutorSnapshot snapshot();
}
