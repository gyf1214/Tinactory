package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftExecutor {
    void start(CraftPlan plan);

    void restore(HolderLookup.Provider provider, CompoundTag tag, PatternCodec codec);

    void runCycle(long itemBandwidth, long fluidBandwidth);

    void cancel();

    boolean isBusy();

    JobState state();

    ExecutionError error();

    int completedSteps();

    int totalSteps();

    CompoundTag serialize(HolderLookup.Provider provider, PatternCodec codec);
}
