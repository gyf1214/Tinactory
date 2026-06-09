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
import java.util.UUID;

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

    public ExecutorSnapshot(
        JobState state,
        ExecutionPhase phase,
        ExecutionError error,
        @Nullable JobState stateAfterFlush,
        CraftPlan plan,
        int nextStepIndex,
        Map<IStackKey, Long> stepBuffer,
        Map<IStackKey, Long> stepProducedOutputs,
        Map<IStackKey, Long> stepRequiredOutputs,
        Map<IStackKey, Long> stepRequiredInputs,
        Map<IStackKey, Long> transmittedInputs,
        Map<IStackKey, Long> transmittedRequiredOutputs,
        @Nullable UUID leasedMachineId) {

        this(
            state,
            phase,
            error,
            stateAfterFlush,
            plan,
            Map.of(),
            stepBuffer,
            nextStepIndex,
            legacyRuntime(
                plan,
                nextStepIndex,
                stepProducedOutputs,
                stepRequiredOutputs,
                stepRequiredInputs,
                transmittedInputs,
                transmittedRequiredOutputs,
                leasedMachineId));
    }

    public int nextStepIndex() {
        return nextUnscheduledStepIndex;
    }

    public Map<IStackKey, Long> stepBuffer() {
        return sharedBuffer;
    }

    private static List<StepRuntime.Snapshot> legacyRuntime(
        CraftPlan plan,
        int nextStepIndex,
        Map<IStackKey, Long> stepProducedOutputs,
        Map<IStackKey, Long> stepRequiredOutputs,
        Map<IStackKey, Long> stepRequiredInputs,
        Map<IStackKey, Long> transmittedInputs,
        Map<IStackKey, Long> transmittedRequiredOutputs,
        @Nullable UUID leasedMachineId) {

        if (nextStepIndex >= plan.steps().size() ||
            leasedMachineId == null &&
                stepProducedOutputs.isEmpty() &&
                stepRequiredOutputs.isEmpty() &&
                stepRequiredInputs.isEmpty() &&
                transmittedInputs.isEmpty() &&
                transmittedRequiredOutputs.isEmpty()) {
            return List.of();
        }
        return List.of(new StepRuntime.Snapshot(
            nextStepIndex,
            leasedMachineId,
            stepRequiredInputs,
            stepRequiredOutputs,
            stepProducedOutputs,
            transmittedInputs,
            transmittedRequiredOutputs,
            null,
            true));
    }
}
