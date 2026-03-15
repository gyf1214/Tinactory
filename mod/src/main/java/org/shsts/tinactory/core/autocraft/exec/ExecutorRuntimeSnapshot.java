package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

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
    Map<IIngredientKey, Long> stepBuffer,
    Map<IIngredientKey, Long> stepProducedOutputs,
    Map<IIngredientKey, Long> stepRequiredOutputs,
    Map<IIngredientKey, Long> stepRequiredInputs,
    Map<IIngredientKey, Long> transmittedInputs,
    Map<IIngredientKey, Long> transmittedRequiredOutputs,
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
