package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class StepRuntime {
    private final int stepIndex;
    private final CraftStep step;
    private final Map<IStackKey, Long> requiredInputs = new HashMap<>();
    private final Map<IStackKey, Long> requiredOutputs = new HashMap<>();
    private final Map<IStackKey, Long> producedOutputs = new HashMap<>();
    private final Map<IStackKey, Long> transmittedInputs = new HashMap<>();
    private final Map<IStackKey, Long> transmittedRequiredOutputs = new HashMap<>();
    @Nullable
    private IMachineLease lease;
    @Nullable
    private UUID leasedMachineId;
    @Nullable
    private ExecutionError blockReason;
    private boolean started;

    public StepRuntime(int stepIndex, CraftStep step, IMachineLease lease) {
        this.stepIndex = stepIndex;
        this.step = step;
        bindLease(lease);
        initializeRequiredInputs();
    }

    public StepRuntime(CraftPlanStep planStep, Snapshot snapshot) {
        stepIndex = snapshot.stepIndex();
        step = planStep.step();
        requiredInputs.putAll(snapshot.requiredInputs());
        requiredOutputs.putAll(snapshot.requiredOutputs());
        producedOutputs.putAll(snapshot.producedOutputs());
        transmittedInputs.putAll(snapshot.transmittedInputs());
        transmittedRequiredOutputs.putAll(snapshot.transmittedRequiredOutputs());
        leasedMachineId = snapshot.leasedMachineId();
        blockReason = snapshot.blockReason();
        started = snapshot.started();
    }

    public int stepIndex() {
        return stepIndex;
    }

    public CraftStep step() {
        return step;
    }

    @Nullable
    public IMachineLease lease() {
        return lease;
    }

    @Nullable
    public UUID leasedMachineId() {
        return leasedMachineId;
    }

    @Nullable
    public ExecutionError blockReason() {
        return blockReason;
    }

    public boolean started() {
        return started;
    }

    public void markStarted() {
        started = true;
    }

    public void clearBlock() {
        blockReason = null;
    }

    public void block(ExecutionError reason) {
        blockReason = reason;
    }

    public void bindLease(IMachineLease lease) {
        this.lease = lease;
        leasedMachineId = lease.machineId();
        initializeRequiredOutputs(lease.outputRoutes());
        blockReason = null;
    }

    public void clearLease() {
        lease = null;
    }

    public void releaseLease() {
        if (lease != null) {
            lease.release();
            lease = null;
        }
    }

    public boolean hasValidLease() {
        return lease != null && lease.isValid();
    }

    public boolean canReassignAfterMachineLoss() {
        var inputsPerRun = aggregateAmounts(step.pattern().inputs(), 1L);
        var outputsPerRun = aggregateAmounts(step.pattern().outputs(), 1L);
        long scheduledRuns = 0L;
        for (var input : inputsPerRun.entrySet()) {
            var requiredPerRun = input.getValue();
            if (requiredPerRun <= 0L) {
                continue;
            }
            var runs = divideCeil(transmittedInputs.getOrDefault(input.getKey(), 0L), requiredPerRun);
            if (runs > scheduledRuns) {
                scheduledRuns = runs;
            }
        }
        long recoveredRuns = Long.MAX_VALUE;
        if (outputsPerRun.isEmpty()) {
            recoveredRuns = 0L;
        }
        for (var output : outputsPerRun.entrySet()) {
            var requiredPerRun = output.getValue();
            if (requiredPerRun <= 0L) {
                continue;
            }
            var runs = transmittedRequiredOutputs.getOrDefault(output.getKey(), 0L) / requiredPerRun;
            if (runs < recoveredRuns) {
                recoveredRuns = runs;
            }
        }
        return scheduledRuns <= recoveredRuns;
    }

    public boolean transfer(PortType type, long bandwidth, Map<IStackKey, Long> sharedBuffer) {
        if (lease == null || bandwidth <= 0L) {
            return false;
        }
        var before = copyBuffer(sharedBuffer);
        var remaining = pullOutputs(type, bandwidth, sharedBuffer);
        if (remaining > 0L) {
            pushInputs(type, remaining, sharedBuffer);
        }
        return !before.equals(sharedBuffer);
    }

    public boolean completed() {
        for (var required : requiredInputs.entrySet()) {
            if (transmittedInputs.getOrDefault(required.getKey(), 0L) < required.getValue()) {
                return false;
            }
        }
        for (var required : requiredOutputs.entrySet()) {
            if (transmittedRequiredOutputs.getOrDefault(required.getKey(), 0L) < required.getValue()) {
                return false;
            }
        }
        return started;
    }

    public Snapshot snapshot() {
        return new Snapshot(
            stepIndex,
            leasedMachineId,
            requiredInputs,
            requiredOutputs,
            producedOutputs,
            transmittedInputs,
            transmittedRequiredOutputs,
            blockReason,
            started);
    }

    private void initializeRequiredInputs() {
        if (!requiredInputs.isEmpty()) {
            return;
        }
        for (var input : step.pattern().inputs()) {
            requiredInputs.merge(input.key(), input.amount() * step.runs(), Long::sum);
        }
    }

    private void initializeRequiredOutputs(List<IMachineRoute> outputRoutes) {
        if (!requiredOutputs.isEmpty()) {
            return;
        }
        var routedOutputs = new HashMap<IStackKey, Boolean>();
        for (var route : outputRoutes) {
            routedOutputs.put(route.key(), true);
        }
        for (var output : step.pattern().outputs()) {
            if (routedOutputs.containsKey(output.key())) {
                requiredOutputs.merge(output.key(), output.amount() * step.runs(), Math::max);
            }
        }
    }

    private long pullOutputs(PortType type, long bandwidth, Map<IStackKey, Long> sharedBuffer) {
        var currentLease = lease;
        if (currentLease == null) {
            return bandwidth;
        }
        var remaining = bandwidth;
        for (var route : currentLease.outputRoutes()) {
            if (remaining <= 0L) {
                break;
            }
            var key = route.key();
            if (key.type() != type) {
                continue;
            }
            var needed = requiredOutputs.getOrDefault(key, 0L) -
                transmittedRequiredOutputs.getOrDefault(key, 0L);
            if (needed <= 0L) {
                continue;
            }
            var moved = route.transfer(Math.min(remaining, needed), false);
            if (moved <= 0L) {
                continue;
            }
            sharedBuffer.merge(key, moved, Long::sum);
            producedOutputs.merge(key, moved, Long::sum);
            transmittedRequiredOutputs.merge(key, moved, Long::sum);
            remaining -= moved;
        }
        return remaining;
    }

    private void pushInputs(PortType type, long bandwidth, Map<IStackKey, Long> sharedBuffer) {
        var currentLease = lease;
        if (currentLease == null) {
            return;
        }
        var remaining = bandwidth;
        var movedAny = false;
        for (var route : currentLease.inputRoutes()) {
            if (remaining <= 0L) {
                break;
            }
            var key = route.key();
            if (key.type() != type) {
                continue;
            }
            var needed = requiredInputs.getOrDefault(key, 0L) - transmittedInputs.getOrDefault(key, 0L);
            if (needed <= 0L) {
                continue;
            }
            var buffered = sharedBuffer.getOrDefault(key, 0L);
            if (buffered <= 0L) {
                continue;
            }
            var moved = route.transfer(Math.min(Math.min(remaining, buffered), needed), false);
            if (moved <= 0L) {
                continue;
            }
            var left = buffered - moved;
            if (left <= 0L) {
                sharedBuffer.remove(key);
            } else {
                sharedBuffer.put(key, left);
            }
            transmittedInputs.merge(key, moved, Long::sum);
            remaining -= moved;
            movedAny = true;
        }
        if (movedAny || completed()) {
            blockReason = null;
        } else if (hasMissingInputs(type)) {
            blockReason = ExecutionError.INPUT_UNAVAILABLE;
        }
    }

    private boolean hasMissingInputs(PortType type) {
        for (var required : requiredInputs.entrySet()) {
            if (required.getKey().type() == type &&
                transmittedInputs.getOrDefault(required.getKey(), 0L) < required.getValue()) {
                return true;
            }
        }
        return false;
    }

    private static Map<IStackKey, Long> aggregateAmounts(List<CraftAmount> amounts, long multiplier) {
        var out = new HashMap<IStackKey, Long>();
        for (var amount : amounts) {
            out.merge(amount.key(), amount.amount() * multiplier, Long::sum);
        }
        return out;
    }

    private static Map<IStackKey, Long> copyBuffer(Map<IStackKey, Long> buffer) {
        return new HashMap<>(buffer);
    }

    private static long divideCeil(long numerator, long denominator) {
        if (denominator <= 0L || numerator <= 0L) {
            return 0L;
        }
        return (numerator + denominator - 1L) / denominator;
    }

    public record Snapshot(
        int stepIndex,
        @Nullable UUID leasedMachineId,
        Map<IStackKey, Long> requiredInputs,
        Map<IStackKey, Long> requiredOutputs,
        Map<IStackKey, Long> producedOutputs,
        Map<IStackKey, Long> transmittedInputs,
        Map<IStackKey, Long> transmittedRequiredOutputs,
        @Nullable ExecutionError blockReason,
        boolean started) {

        public Snapshot {
            requiredInputs = Map.copyOf(requiredInputs);
            requiredOutputs = Map.copyOf(requiredOutputs);
            producedOutputs = Map.copyOf(producedOutputs);
            transmittedInputs = Map.copyOf(transmittedInputs);
            transmittedRequiredOutputs = Map.copyOf(transmittedRequiredOutputs);
        }
    }

    public record CraftPlanStep(CraftStep step) {}
}
