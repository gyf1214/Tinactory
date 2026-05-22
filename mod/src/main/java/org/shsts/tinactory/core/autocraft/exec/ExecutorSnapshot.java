package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

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
    int nextStepIndex,
    Map<IStackKey, Long> pendingFlush,
    boolean flushStepBufferInPhase,
    Map<IStackKey, Long> stepBuffer,
    Map<IStackKey, Long> stepProducedOutputs,
    Map<IStackKey, Long> stepRequiredOutputs,
    Map<IStackKey, Long> stepRequiredInputs,
    Map<IStackKey, Long> transmittedInputs,
    Map<IStackKey, Long> transmittedRequiredOutputs,
    @Nullable UUID leasedMachineId) {

    public ExecutorSnapshot {
        pendingFlush = Map.copyOf(pendingFlush);
        stepBuffer = Map.copyOf(stepBuffer);
        stepProducedOutputs = Map.copyOf(stepProducedOutputs);
        stepRequiredOutputs = Map.copyOf(stepRequiredOutputs);
        stepRequiredInputs = Map.copyOf(stepRequiredInputs);
        transmittedInputs = Map.copyOf(transmittedInputs);
        transmittedRequiredOutputs = Map.copyOf(transmittedRequiredOutputs);
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
            nextStepIndex,
            Map.of(),
            phase == ExecutionPhase.FLUSHING && stateAfterFlush == JobState.IDLE,
            stepBuffer,
            stepProducedOutputs,
            stepRequiredOutputs,
            stepRequiredInputs,
            transmittedInputs,
            transmittedRequiredOutputs,
            leasedMachineId);
    }
}
