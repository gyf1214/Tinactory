package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftExecutor {
    void start(CraftPlan plan);

    void restore(CompoundTag tag, PatternNbtCodec codec);

    void runCycle(long transmissionBandwidth);

    void cancel();

    boolean isBusy();

    JobState state();

    ExecutionError error();

    int completedSteps();

    int totalSteps();

    CompoundTag serialize(PatternNbtCodec codec);
}
