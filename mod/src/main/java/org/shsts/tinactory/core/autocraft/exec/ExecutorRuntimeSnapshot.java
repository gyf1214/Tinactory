package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;

import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ExecutorRuntimeSnapshot(
    ExecutionState state,
    ExecutionDetails.Phase phase,
    @Nullable ExecutionError error,
    @Nullable ExecutionError.Code blockedReason,
    @Nullable ExecutionState pendingTerminalState,
    int nextStepIndex,
    Map<CraftKey, Long> stepBuffer,
    Map<CraftKey, Long> stepProducedOutputs,
    Map<CraftKey, Long> stepRequiredOutputs,
    Map<CraftKey, Long> stepRequiredInputs,
    Map<CraftKey, Long> transmittedInputs,
    Map<CraftKey, Long> transmittedRequiredOutputs,
    @Nullable UUID leasedMachineId) {

    public ExecutorRuntimeSnapshot {
        stepBuffer = Map.copyOf(stepBuffer);
        stepProducedOutputs = Map.copyOf(stepProducedOutputs);
        stepRequiredOutputs = Map.copyOf(stepRequiredOutputs);
        stepRequiredInputs = Map.copyOf(stepRequiredInputs);
        transmittedInputs = Map.copyOf(transmittedInputs);
        transmittedRequiredOutputs = Map.copyOf(transmittedRequiredOutputs);
    }
}
