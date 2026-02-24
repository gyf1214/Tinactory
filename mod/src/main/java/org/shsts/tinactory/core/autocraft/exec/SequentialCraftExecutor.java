package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SequentialCraftExecutor implements ICraftExecutor {
    private final IInventoryView inventory;
    private final IMachineAllocator machineAllocator;
    private final IJobEvents jobEvents;

    private CraftPlan plan = new CraftPlan(List.of());
    private int nextStep = 0;
    private ExecutionState state = ExecutionState.IDLE;
    private ExecutionDetails.Phase phase = ExecutionDetails.Phase.TERMINAL;
    @Nullable
    private ExecutionState pendingTerminalState;
    @Nullable
    private ExecutionError error;
    @Nullable
    private ExecutionError.Code blockedReason;
    @Nullable
    private IMachineLease lease;
    @Nullable
    private UUID leasedMachineId;

    private final Map<CraftKey, Long> stepBuffer = new HashMap<>();
    private final Map<CraftKey, Long> stepProducedOutputs = new HashMap<>();
    private final Map<CraftKey, Long> stepRequiredOutputs = new HashMap<>();
    private final Map<CraftKey, Long> stepRequiredInputs = new HashMap<>();
    private final Map<CraftKey, Long> transmittedInputs = new HashMap<>();
    private final Map<CraftKey, Long> transmittedRequiredOutputs = new HashMap<>();
    private final Map<CraftKey, Long> pendingFlush = new HashMap<>();
    private boolean flushStepBufferInPhase;

    public SequentialCraftExecutor(IInventoryView inventory, IMachineAllocator machineAllocator, IJobEvents jobEvents) {
        this.inventory = inventory;
        this.machineAllocator = machineAllocator;
        this.jobEvents = jobEvents;
    }

    @Override
    public void start(CraftPlan plan) {
        start(plan, 0, null);
    }

    public void start(CraftPlan plan, int nextStepIndex, @Nullable ExecutorRuntimeSnapshot snapshot) {
        this.plan = plan;
        nextStep = nextStepIndex;
        if (plan.steps().isEmpty() || nextStep >= plan.steps().size()) {
            state = ExecutionState.COMPLETED;
        } else {
            state = ExecutionState.RUNNING;
        }
        phase = state == ExecutionState.RUNNING ? ExecutionDetails.Phase.RUN_STEP : ExecutionDetails.Phase.TERMINAL;
        error = null;
        blockedReason = null;
        pendingTerminalState = null;
        releaseLease();
        clearStepState();
        flushStepBufferInPhase = false;

        if (snapshot != null) {
            restoreSnapshot(snapshot);
        }
    }

    @Override
    public void runCycle(long transmissionBandwidth) {
        if (phase == ExecutionDetails.Phase.TERMINAL ||
            state == ExecutionState.COMPLETED ||
            state == ExecutionState.CANCELLED ||
            state == ExecutionState.FAILED) {
            return;
        }
        if (phase == ExecutionDetails.Phase.FLUSHING) {
            flushStepBuffer(transmissionBandwidth);
            return;
        }
        if (nextStep >= plan.steps().size()) {
            beginFlushing(ExecutionState.COMPLETED, null, null, true);
            flushStepBuffer(transmissionBandwidth);
            return;
        }

        var step = plan.steps().get(nextStep);
        if (!ensureStepReady()) {
            jobEvents.onStepBlocked(step, error == null ? "step blocked" : error.message());
            return;
        }
        if (!ensureLease(step)) {
            jobEvents.onStepBlocked(step, error == null ? "machine unavailable" : error.message());
            return;
        }

        if (transmissionBandwidth > 0L) {
            var remaining = pullOutputs(transmissionBandwidth);
            if (remaining > 0L) {
                pushInputs(remaining);
            }
        }

        if (stepCompleted()) {
            releaseLease();
            var completedStep = step;
            var flushCandidates = extractFlushCandidates(completedStep);
            clearStepProgressState();
            jobEvents.onStepCompleted(completedStep);
            nextStep++;
            blockedReason = null;
            error = null;
            state = ExecutionState.RUNNING;

            if (!flushStepBufferNow(flushCandidates)) {
                pendingFlush.putAll(flushCandidates);
                phase = ExecutionDetails.Phase.FLUSHING;
                pendingTerminalState = ExecutionState.RUNNING;
                state = ExecutionState.BLOCKED;
                blockedReason = ExecutionError.Code.FLUSH_BACKPRESSURE;
                error = new ExecutionError(
                    ExecutionError.Code.FLUSH_BACKPRESSURE,
                    completedStep.stepId(),
                    "Flush blocked by storage backpressure");
                flushStepBufferInPhase = false;
                return;
            }
            if (nextStep >= plan.steps().size()) {
                beginFlushing(ExecutionState.COMPLETED, null, null, true);
            }
        }
    }

    @Override
    public void cancel() {
        if (phase == ExecutionDetails.Phase.TERMINAL) {
            return;
        }
        beginFlushing(
            ExecutionState.CANCELLED,
            new ExecutionError(ExecutionError.Code.CANCELLED, nextStepId(), "Execution cancelled"),
            null,
            true);
    }

    @Override
    public ExecutionState state() {
        return state;
    }

    @Override
    public @Nullable ExecutionError error() {
        return error;
    }

    @Override
    public ExecutionDetails details() {
        return new ExecutionDetails(
            phase,
            blockedReason,
            pendingTerminalState,
            nextStep,
            stepBuffer,
            transmittedInputs,
            transmittedRequiredOutputs,
            leasedMachineId);
    }

    public CraftPlan currentPlan() {
        return plan;
    }

    public int nextStepIndex() {
        return nextStep;
    }

    public ExecutorRuntimeSnapshot snapshot() {
        return new ExecutorRuntimeSnapshot(
            state,
            phase,
            error,
            blockedReason,
            pendingTerminalState,
            nextStep,
            stepBuffer,
            stepProducedOutputs,
            stepRequiredOutputs,
            stepRequiredInputs,
            transmittedInputs,
            transmittedRequiredOutputs,
            leasedMachineId);
    }

    private void restoreSnapshot(ExecutorRuntimeSnapshot snapshot) {
        state = snapshot.state();
        phase = snapshot.phase();
        error = snapshot.error();
        blockedReason = snapshot.blockedReason();
        pendingTerminalState = snapshot.pendingTerminalState();
        nextStep = snapshot.nextStepIndex();
        stepBuffer.putAll(snapshot.stepBuffer());
        stepProducedOutputs.putAll(snapshot.stepProducedOutputs());
        stepRequiredOutputs.putAll(snapshot.stepRequiredOutputs());
        stepRequiredInputs.putAll(snapshot.stepRequiredInputs());
        transmittedInputs.putAll(snapshot.transmittedInputs());
        transmittedRequiredOutputs.putAll(snapshot.transmittedRequiredOutputs());
        leasedMachineId = snapshot.leasedMachineId();
    }

    private boolean ensureStepReady() {
        if (!stepRequiredInputs.isEmpty()) {
            return true;
        }
        var step = plan.steps().get(nextStep);
        for (var output : step.requiredOutputs()) {
            stepRequiredOutputs.merge(output.key(), output.amount(), Long::sum);
        }
        for (var input : step.pattern().inputs()) {
            var amount = input.amount() * step.runs();
            stepRequiredInputs.merge(input.key(), amount, Long::sum);
        }

        var inventoryReservations = new HashMap<CraftKey, Long>();
        var bufferedReservations = new HashMap<CraftKey, Long>();
        for (var required : stepRequiredInputs.entrySet()) {
            var buffered = stepBuffer.getOrDefault(required.getKey(), 0L);
            var missing = required.getValue() - Math.min(required.getValue(), buffered);
            if (missing <= 0L) {
                continue;
            }
            var simulated = inventory.extract(required.getKey(), missing, true);
            if (simulated < missing) {
                rollbackReservations(inventoryReservations);
                rollbackBufferedReservations(bufferedReservations);
                blockStep(ExecutionError.Code.INPUT_UNAVAILABLE, "Input resources are unavailable");
                return false;
            }
            var extracted = inventory.extract(required.getKey(), missing, false);
            if (extracted < missing) {
                if (extracted > 0L) {
                    inventory.insert(required.getKey(), extracted, false);
                }
                rollbackReservations(inventoryReservations);
                rollbackBufferedReservations(bufferedReservations);
                blockStep(ExecutionError.Code.INPUT_UNAVAILABLE, "Input resources are unavailable");
                return false;
            }
            inventoryReservations.put(required.getKey(), extracted);
            bufferedReservations.put(required.getKey(), extracted);
            stepBuffer.merge(required.getKey(), extracted, Long::sum);
        }
        jobEvents.onStepStarted(step);
        blockedReason = null;
        error = null;
        state = ExecutionState.RUNNING;
        return true;
    }

    private boolean ensureLease(CraftStep step) {
        if (lease != null) {
            if (lease.isValid()) {
                return true;
            }
            if (!canReassignAfterMachineLoss()) {
                blockStep(
                    ExecutionError.Code.MACHINE_REASSIGNMENT_BLOCKED,
                    "Machine reassignment blocked by in-flight transfer");
                return false;
            }
            releaseLease();
        }

        var allocated = machineAllocator.allocate(step);
        if (allocated.isEmpty()) {
            blockStep(ExecutionError.Code.MACHINE_UNAVAILABLE, "Machine requirement is unavailable");
            return false;
        }
        lease = allocated.get();
        leasedMachineId = lease.machineId();
        blockedReason = null;
        error = null;
        state = ExecutionState.RUNNING;
        return true;
    }

    private long pullOutputs(long bandwidth) {
        if (lease == null) {
            return bandwidth;
        }
        var remaining = bandwidth;
        for (var route : lease.outputRoutes()) {
            if (remaining <= 0L) {
                break;
            }
            var key = route.key();
            var needed = stepRequiredOutputs.getOrDefault(key, 0L) - transmittedRequiredOutputs.getOrDefault(key, 0L);
            if (needed <= 0L) {
                continue;
            }
            var moved = route.pull(Math.min(remaining, needed), false);
            if (moved <= 0L) {
                continue;
            }
            stepBuffer.merge(key, moved, Long::sum);
            stepProducedOutputs.merge(key, moved, Long::sum);
            transmittedRequiredOutputs.merge(key, moved, Long::sum);
            remaining -= moved;
        }
        if (remaining <= 0L) {
            return 0L;
        }
        for (var route : lease.outputRoutes()) {
            if (remaining <= 0L) {
                break;
            }
            var moved = route.pull(remaining, false);
            if (moved <= 0L) {
                continue;
            }
            stepBuffer.merge(route.key(), moved, Long::sum);
            stepProducedOutputs.merge(route.key(), moved, Long::sum);
            remaining -= moved;
        }
        return remaining;
    }

    private void pushInputs(long bandwidth) {
        if (lease == null) {
            return;
        }
        var remaining = bandwidth;
        for (var route : lease.inputRoutes()) {
            if (remaining <= 0L) {
                break;
            }
            var key = route.key();
            var buffered = stepBuffer.getOrDefault(key, 0L);
            if (buffered <= 0L) {
                continue;
            }
            var moved = route.push(Math.min(remaining, buffered), false);
            if (moved <= 0L) {
                continue;
            }
            stepBuffer.put(key, buffered - moved);
            if (stepBuffer.get(key) == 0L) {
                stepBuffer.remove(key);
            }
            transmittedInputs.merge(key, moved, Long::sum);
            remaining -= moved;
        }
    }

    private boolean stepCompleted() {
        for (var required : stepRequiredOutputs.entrySet()) {
            if (transmittedRequiredOutputs.getOrDefault(required.getKey(), 0L) < required.getValue()) {
                return false;
            }
        }
        return !stepRequiredOutputs.isEmpty();
    }

    private boolean canReassignAfterMachineLoss() {
        long scheduledRuns = 0L;
        for (var input : stepRequiredInputs.entrySet()) {
            var requiredPerStep = input.getValue();
            if (requiredPerStep <= 0L) {
                continue;
            }
            var transmitted = transmittedInputs.getOrDefault(input.getKey(), 0L);
            var runs = divideCeil(transmitted, requiredPerStep);
            if (runs > scheduledRuns) {
                scheduledRuns = runs;
            }
        }

        long recoveredRuns = Long.MAX_VALUE;
        if (stepRequiredOutputs.isEmpty()) {
            recoveredRuns = 0L;
        }
        for (var output : stepRequiredOutputs.entrySet()) {
            var requiredPerStep = output.getValue();
            if (requiredPerStep <= 0L) {
                continue;
            }
            var transmitted = transmittedRequiredOutputs.getOrDefault(output.getKey(), 0L);
            var runs = transmitted / requiredPerStep;
            if (runs < recoveredRuns) {
                recoveredRuns = runs;
            }
        }
        return scheduledRuns <= recoveredRuns;
    }

    private void beginFlushing(
        ExecutionState terminalState,
        @Nullable ExecutionError terminalError,
        @Nullable ExecutionError.Code flushBlockedReason,
        boolean includeStepBuffer) {
        releaseLease();
        pendingTerminalState = terminalState;
        phase = ExecutionDetails.Phase.FLUSHING;
        error = terminalError;
        blockedReason = flushBlockedReason;
        flushStepBufferInPhase = includeStepBuffer;
        state = flushBlockedReason == null ? ExecutionState.RUNNING : ExecutionState.BLOCKED;
    }

    private void flushStepBuffer(long bandwidth) {
        if (phase != ExecutionDetails.Phase.FLUSHING) {
            return;
        }
        var remaining = bandwidth;
        remaining = flushAmounts(pendingFlush, remaining);
        if (flushStepBufferInPhase && remaining > 0L) {
            remaining = flushAmounts(stepBuffer, remaining);
        }

        var done = pendingFlush.isEmpty() && (!flushStepBufferInPhase || stepBuffer.isEmpty());
        if (done) {
            var terminalState = pendingTerminalState == null ? ExecutionState.COMPLETED : pendingTerminalState;
            state = terminalState;
            blockedReason = null;
            pendingTerminalState = null;
            if (terminalState == ExecutionState.RUNNING) {
                phase = ExecutionDetails.Phase.RUN_STEP;
                if (error != null && error.code() == ExecutionError.Code.FLUSH_BACKPRESSURE) {
                    error = null;
                }
            } else {
                phase = ExecutionDetails.Phase.TERMINAL;
                clearStepState();
            }
            flushStepBufferInPhase = false;
            return;
        }
        if (remaining > 0L) {
            blockedReason = ExecutionError.Code.FLUSH_BACKPRESSURE;
            error = new ExecutionError(
                ExecutionError.Code.FLUSH_BACKPRESSURE,
                nextStepId(),
                "Flush blocked by storage backpressure");
            state = ExecutionState.BLOCKED;
        } else {
            blockedReason = null;
            if (error != null && error.code() == ExecutionError.Code.FLUSH_BACKPRESSURE) {
                error = null;
            }
            state = ExecutionState.RUNNING;
        }
    }

    private long flushAmounts(Map<CraftKey, Long> amounts, long bandwidth) {
        var remaining = bandwidth;
        for (var entry : List.copyOf(amounts.entrySet())) {
            if (remaining <= 0L) {
                break;
            }
            var target = Math.min(remaining, entry.getValue());
            var inserted = inventory.insert(entry.getKey(), target, false);
            if (inserted <= 0L) {
                continue;
            }
            var left = entry.getValue() - inserted;
            if (left <= 0L) {
                amounts.remove(entry.getKey());
            } else {
                amounts.put(entry.getKey(), left);
            }
            remaining -= inserted;
        }
        return remaining;
    }

    private boolean flushStepBufferNow(Map<CraftKey, Long> amounts) {
        for (var entry : List.copyOf(amounts.entrySet())) {
            var inserted = inventory.insert(entry.getKey(), entry.getValue(), false);
            if (inserted < entry.getValue()) {
                amounts.put(entry.getKey(), entry.getValue() - inserted);
                return false;
            }
            amounts.remove(entry.getKey());
        }
        return true;
    }

    private void clearStepState() {
        stepBuffer.clear();
        clearStepProgressState();
        pendingFlush.clear();
        flushStepBufferInPhase = false;
    }

    private void clearStepProgressState() {
        stepProducedOutputs.clear();
        stepRequiredOutputs.clear();
        stepRequiredInputs.clear();
        transmittedInputs.clear();
        transmittedRequiredOutputs.clear();
        leasedMachineId = null;
    }

    private void rollbackReservations(Map<CraftKey, Long> reservations) {
        for (var reserved : reservations.entrySet()) {
            if (reserved.getValue() > 0L) {
                inventory.insert(reserved.getKey(), reserved.getValue(), false);
            }
        }
    }

    private void rollbackBufferedReservations(Map<CraftKey, Long> reservations) {
        for (var reserved : reservations.entrySet()) {
            var buffered = stepBuffer.getOrDefault(reserved.getKey(), 0L);
            var retained = buffered - reserved.getValue();
            if (retained > 0L) {
                stepBuffer.put(reserved.getKey(), retained);
            } else {
                stepBuffer.remove(reserved.getKey());
            }
        }
    }

    private void blockStep(ExecutionError.Code code, String message) {
        error = new ExecutionError(code, nextStepId(), message);
        blockedReason = code;
        state = ExecutionState.BLOCKED;
    }

    private String nextStepId() {
        if (nextStep >= plan.steps().size()) {
            return "complete";
        }
        return plan.steps().get(nextStep).stepId();
    }

    private void releaseLease() {
        if (lease != null) {
            lease.release();
            lease = null;
        }
        leasedMachineId = null;
    }

    private Map<CraftKey, Long> extractFlushCandidates(CraftStep step) {
        var requiredIntermediateByKey = new HashMap<CraftKey, Long>();
        for (var amount : step.requiredIntermediateOutputs()) {
            requiredIntermediateByKey.merge(amount.key(), amount.amount(), Long::sum);
        }
        var flushCandidates = new HashMap<CraftKey, Long>();
        for (var produced : stepProducedOutputs.entrySet()) {
            var key = produced.getKey();
            var buffered = stepBuffer.getOrDefault(key, 0L);
            if (buffered <= 0L) {
                continue;
            }
            var flush = produced.getValue() - requiredIntermediateByKey.getOrDefault(key, 0L);
            if (flush <= 0L) {
                continue;
            }
            flush = Math.min(flush, buffered);
            var retained = buffered - flush;
            if (retained > 0L) {
                stepBuffer.put(key, retained);
            } else {
                stepBuffer.remove(key);
            }
            if (flush > 0L) {
                flushCandidates.put(key, flush);
            }
        }
        return flushCandidates;
    }

    private static long divideCeil(long numerator, long denominator) {
        if (denominator <= 0L || numerator <= 0L) {
            return 0L;
        }
        return (numerator + denominator - 1L) / denominator;
    }
}
