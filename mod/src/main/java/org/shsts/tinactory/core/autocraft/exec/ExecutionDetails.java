package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ExecutionDetails(
    Phase phase,
    @Nullable ExecutionError.Code blockedReason,
    @Nullable ExecutionState pendingTerminalState,
    int nextStepIndex,
    Map<CraftKey, Long> stepBuffer,
    Map<CraftKey, Long> transmittedInputs,
    Map<CraftKey, Long> transmittedRequiredOutputs,
    @Nullable UUID leasedMachineId) {

    public ExecutionDetails {
        stepBuffer = Map.copyOf(stepBuffer);
        transmittedInputs = Map.copyOf(transmittedInputs);
        transmittedRequiredOutputs = Map.copyOf(transmittedRequiredOutputs);
    }

    public enum Phase {
        RUN_STEP,
        FLUSHING,
        TERMINAL
    }
}
