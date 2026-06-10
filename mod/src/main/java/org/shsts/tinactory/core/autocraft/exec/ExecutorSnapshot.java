package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ExecutorSnapshot(
    JobState state,
    ExecutionPhase phase,
    ExecutionError error,
    @Nullable JobState stateAfterFlush,
    CraftPlan plan,
    Map<IStackKey, Long> requiredInventory,
    Map<IStackKey, Long> sharedBuffer,
    int nextUnscheduledStepIndex,
    List<StepRuntime.Snapshot> activeRuntimes) {

    public ExecutorSnapshot {
        requiredInventory = Map.copyOf(requiredInventory);
        sharedBuffer = Map.copyOf(sharedBuffer);
        activeRuntimes = List.copyOf(activeRuntimes);
    }
}
