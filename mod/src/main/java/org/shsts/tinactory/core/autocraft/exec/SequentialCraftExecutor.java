package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.nbt.Tag.TAG_LIST;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SequentialCraftExecutor implements ICraftExecutor {
    private final IInventoryView inventory;
    private final IMachineAllocator machineAllocator;
    private final IJobEvents jobEvents;

    private CraftPlan plan = new CraftPlan(List.of());
    private int nextStep = 0;
    private JobState state = JobState.IDLE;
    private ExecutionPhase phase = ExecutionPhase.TERMINAL;
    @Nullable
    private JobState stateAfterFlush;
    private ExecutionError error = ExecutionError.NONE;
    @Nullable
    private IMachineLease lease;
    @Nullable
    private UUID leasedMachineId;

    private final Map<IStackKey, Long> stepBuffer = new HashMap<>();
    private final Map<IStackKey, Long> stepProducedOutputs = new HashMap<>();
    private final Map<IStackKey, Long> stepRequiredOutputs = new HashMap<>();
    private final Map<IStackKey, Long> stepRequiredInputs = new HashMap<>();
    private final Map<IStackKey, Long> transmittedInputs = new HashMap<>();
    private final Map<IStackKey, Long> transmittedRequiredOutputs = new HashMap<>();
    private final Map<IStackKey, Long> pendingFlush = new HashMap<>();
    private boolean flushStepBufferInPhase;

    public SequentialCraftExecutor(IInventoryView inventory, IMachineAllocator machineAllocator, IJobEvents jobEvents) {
        this.inventory = inventory;
        this.machineAllocator = machineAllocator;
        this.jobEvents = jobEvents;
    }

    @Override
    public void start(CraftPlan plan) {
        ensureNotActive("start");
        initializeRun(plan, 0);
    }

    public void restore(ExecutorSnapshot snapshot) {
        ensureNotActive("restore");
        initializeRun(snapshot.plan(), snapshot.nextStepIndex());
        restoreSnapshot(snapshot);
    }

    @Override
    public void restore(CompoundTag tag, PatternNbtCodec codec) {
        restore(deserialize(tag, codec));
    }

    private void initializeRun(CraftPlan plan, int nextStepIndex) {
        this.plan = plan;
        nextStep = nextStepIndex;
        if (plan.steps().isEmpty() || nextStep >= plan.steps().size()) {
            state = JobState.IDLE;
        } else {
            state = JobState.RUNNING;
        }
        phase = state == JobState.RUNNING ? ExecutionPhase.RUN_STEP : ExecutionPhase.TERMINAL;
        error = ExecutionError.NONE;
        stateAfterFlush = null;
        releaseLease();
        clearStepState();
        flushStepBufferInPhase = false;
    }

    @Override
    public void runCycle(long transmissionBandwidth) {
        if (phase == ExecutionPhase.TERMINAL ||
            state == JobState.FAILED) {
            return;
        }
        if (phase == ExecutionPhase.FLUSHING) {
            flushStepBuffer(transmissionBandwidth);
            return;
        }
        if (nextStep >= plan.steps().size()) {
            beginFinalFlushing();
            flushStepBuffer(transmissionBandwidth);
            return;
        }

        var step = plan.steps().get(nextStep);
        if (!ensureStepReady()) {
            jobEvents.onStepBlocked(step, error);
            return;
        }
        if (!ensureLease(step)) {
            jobEvents.onStepBlocked(step, error);
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
            var flushCandidates = extractFlushCandidates(step);
            clearStepProgressState();
            jobEvents.onStepCompleted(step);
            nextStep++;
            error = ExecutionError.NONE;
            state = JobState.RUNNING;

            if (!flushStepBufferNow(flushCandidates)) {
                pendingFlush.putAll(flushCandidates);
                phase = ExecutionPhase.FLUSHING;
                stateAfterFlush = JobState.RUNNING;
                state = JobState.BLOCKED;
                error = ExecutionError.FLUSH_BACKPRESSURE;
                flushStepBufferInPhase = false;
                return;
            }
            if (nextStep >= plan.steps().size()) {
                beginFinalFlushing();
            }
        }
    }

    @Override
    public void cancel() {
        if (phase == ExecutionPhase.TERMINAL) {
            return;
        }
        beginFinalFlushing();
    }

    @Override
    public boolean isBusy() {
        return state.busy();
    }

    @Override
    public JobState state() {
        return state;
    }

    @Override
    public ExecutionError error() {
        return error;
    }

    @Override
    public int completedSteps() {
        return nextStep;
    }

    @Override
    public int totalSteps() {
        return plan.steps().size();
    }

    @Override
    public CompoundTag serialize(PatternNbtCodec codec) {
        return serialize(snapshot(), codec);
    }

    public ExecutorSnapshot snapshot() {
        return new ExecutorSnapshot(
            state,
            phase,
            error,
            stateAfterFlush,
            plan,
            nextStep,
            pendingFlush,
            flushStepBufferInPhase,
            stepBuffer,
            stepProducedOutputs,
            stepRequiredOutputs,
            stepRequiredInputs,
            transmittedInputs,
            transmittedRequiredOutputs,
            leasedMachineId);
    }

    private void restoreSnapshot(ExecutorSnapshot snapshot) {
        state = snapshot.state();
        phase = snapshot.phase();
        error = snapshot.error();
        stateAfterFlush = snapshot.stateAfterFlush();
        nextStep = snapshot.nextStepIndex();
        pendingFlush.putAll(snapshot.pendingFlush());
        flushStepBufferInPhase = snapshot.flushStepBufferInPhase();
        stepBuffer.putAll(snapshot.stepBuffer());
        stepProducedOutputs.putAll(snapshot.stepProducedOutputs());
        stepRequiredOutputs.putAll(snapshot.stepRequiredOutputs());
        stepRequiredInputs.putAll(snapshot.stepRequiredInputs());
        transmittedInputs.putAll(snapshot.transmittedInputs());
        transmittedRequiredOutputs.putAll(snapshot.transmittedRequiredOutputs());
        leasedMachineId = snapshot.leasedMachineId();
    }

    private static CompoundTag serialize(ExecutorSnapshot snapshot, PatternNbtCodec codec) {
        var tag = new CompoundTag();
        tag.putString("state", snapshot.state().name());
        tag.putString("phase", snapshot.phase().name());
        tag.put("error", serializeError(snapshot.error()));
        if (snapshot.stateAfterFlush() != null) {
            tag.putString("stateAfterFlush", snapshot.stateAfterFlush().name());
        }
        tag.put("plan", serializePlan(snapshot.plan(), codec));
        tag.putInt("nextStepIndex", snapshot.nextStepIndex());
        tag.put("pendingFlush", serializeKeyedAmounts(snapshot.pendingFlush(), codec));
        tag.putBoolean("flushStepBufferInPhase", snapshot.flushStepBufferInPhase());
        tag.put("stepBuffer", serializeKeyedAmounts(snapshot.stepBuffer(), codec));
        tag.put("stepProducedOutputs", serializeKeyedAmounts(snapshot.stepProducedOutputs(), codec));
        tag.put("stepRequiredOutputs", serializeKeyedAmounts(snapshot.stepRequiredOutputs(), codec));
        tag.put("stepRequiredInputs", serializeKeyedAmounts(snapshot.stepRequiredInputs(), codec));
        tag.put("transmittedInputs", serializeKeyedAmounts(snapshot.transmittedInputs(), codec));
        tag.put("transmittedRequiredOutputs", serializeKeyedAmounts(snapshot.transmittedRequiredOutputs(), codec));
        if (snapshot.leasedMachineId() != null) {
            tag.putUUID("leasedMachineId", snapshot.leasedMachineId());
        }
        return tag;
    }

    private static ExecutorSnapshot deserialize(CompoundTag tag, PatternNbtCodec codec) {
        return new ExecutorSnapshot(
            JobState.valueOf(tag.getString("state")),
            ExecutionPhase.valueOf(tag.getString("phase")),
            tag.contains("error", TAG_COMPOUND) ? deserializeError(tag.getCompound("error")) : ExecutionError.NONE,
            deserializeStateAfterFlush(tag),
            deserializePlan(tag.getCompound("plan"), codec),
            tag.getInt("nextStepIndex"),
            tag.contains("pendingFlush", TAG_LIST) ?
                deserializeKeyedAmounts(tag.getList("pendingFlush", TAG_COMPOUND), codec) :
                Map.of(),
            tag.contains("flushStepBufferInPhase") ?
                tag.getBoolean("flushStepBufferInPhase") :
                shouldFlushStepBufferInPhase(tag),
            deserializeKeyedAmounts(tag.getList("stepBuffer", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("stepProducedOutputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("stepRequiredOutputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("stepRequiredInputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("transmittedInputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("transmittedRequiredOutputs", TAG_COMPOUND), codec),
            tag.hasUUID("leasedMachineId") ? tag.getUUID("leasedMachineId") : null);
    }

    @Nullable
    private static JobState deserializeStateAfterFlush(CompoundTag tag) {
        return tag.contains("stateAfterFlush") ? JobState.valueOf(tag.getString("stateAfterFlush")) : null;
    }

    private static boolean shouldFlushStepBufferInPhase(CompoundTag tag) {
        var stateAfterFlush = deserializeStateAfterFlush(tag);
        return ExecutionPhase.valueOf(tag.getString("phase")) == ExecutionPhase.FLUSHING &&
            stateAfterFlush == JobState.IDLE;
    }

    private static CompoundTag serializePlan(CraftPlan plan, PatternNbtCodec codec) {
        var tag = new CompoundTag();
        var steps = new ListTag();
        for (var step : plan.steps()) {
            var stepTag = new CompoundTag();
            stepTag.putString("stepId", step.stepId());
            stepTag.putLong("runs", step.runs());
            stepTag.put("pattern", codec.encodePattern(step.pattern()));
            stepTag.put("requiredIntermediateOutputs", serializeAmounts(step.requiredIntermediateOutputs(), codec));
            stepTag.put("requiredFinalOutputs", serializeAmounts(step.requiredFinalOutputs(), codec));
            steps.add(stepTag);
        }
        tag.put("steps", steps);
        return tag;
    }

    private static CraftPlan deserializePlan(CompoundTag tag, PatternNbtCodec codec) {
        var steps = tag.getList("steps", TAG_COMPOUND);
        var out = new ArrayList<CraftStep>(steps.size());
        for (var i = 0; i < steps.size(); i++) {
            var stepTag = steps.getCompound(i);
            out.add(new CraftStep(
                stepTag.getString("stepId"),
                codec.decodePattern(stepTag.getCompound("pattern")),
                stepTag.getLong("runs"),
                deserializeAmounts(stepTag.getList("requiredIntermediateOutputs", TAG_COMPOUND), codec),
                deserializeAmounts(stepTag.getList("requiredFinalOutputs", TAG_COMPOUND), codec)));
        }
        return new CraftPlan(out);
    }

    private static ListTag serializeAmounts(List<CraftAmount> amounts, PatternNbtCodec codec) {
        var out = new ListTag();
        for (var amount : amounts) {
            out.add(codec.encodeAmount(amount));
        }
        return out;
    }

    private static List<CraftAmount> deserializeAmounts(ListTag amounts, PatternNbtCodec codec) {
        var out = new ArrayList<CraftAmount>(amounts.size());
        for (var i = 0; i < amounts.size(); i++) {
            out.add(codec.decodeAmount(amounts.getCompound(i)));
        }
        return out;
    }

    private static CompoundTag serializeError(ExecutionError error) {
        var tag = new CompoundTag();
        tag.putString("value", error.name());
        return tag;
    }

    private static ExecutionError deserializeError(CompoundTag tag) {
        return ExecutionError.valueOf(tag.getString("value"));
    }

    private static ListTag serializeKeyedAmounts(Map<IStackKey, Long> amounts, PatternNbtCodec codec) {
        var out = new ListTag();
        for (var entry : amounts.entrySet()) {
            out.add(codec.encodeAmount(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    private static Map<IStackKey, Long> deserializeKeyedAmounts(ListTag tags, PatternNbtCodec codec) {
        var out = new LinkedHashMap<IStackKey, Long>();
        for (var i = 0; i < tags.size(); i++) {
            var amount = codec.decodeAmount(tags.getCompound(i));
            out.put(amount.key(), amount.amount());
        }
        return out;
    }

    private void ensureNotActive(String action) {
        if (state == JobState.RUNNING || state == JobState.BLOCKED) {
            throw new IllegalStateException("cannot " + action + " active executor");
        }
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

        var inventoryReservations = new HashMap<IStackKey, Long>();
        var bufferedReservations = new HashMap<IStackKey, Long>();
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
                blockStep(ExecutionError.INPUT_UNAVAILABLE);
                return false;
            }
            var extracted = inventory.extract(required.getKey(), missing, false);
            if (extracted < missing) {
                if (extracted > 0L) {
                    inventory.insert(required.getKey(), extracted, false);
                }
                rollbackReservations(inventoryReservations);
                rollbackBufferedReservations(bufferedReservations);
                blockStep(ExecutionError.INPUT_UNAVAILABLE);
                return false;
            }
            inventoryReservations.put(required.getKey(), extracted);
            bufferedReservations.put(required.getKey(), extracted);
            stepBuffer.merge(required.getKey(), extracted, Long::sum);
        }
        jobEvents.onStepStarted(step);
        error = ExecutionError.NONE;
        state = JobState.RUNNING;
        return true;
    }

    private boolean ensureLease(CraftStep step) {
        if (lease != null) {
            if (lease.isValid()) {
                return true;
            }
            if (!canReassignAfterMachineLoss()) {
                blockStep(
                    ExecutionError.MACHINE_REASSIGNMENT_BLOCKED);
                return false;
            }
            releaseLease();
        }

        var allocated = machineAllocator.allocate(step);
        if (allocated.isEmpty()) {
            blockStep(ExecutionError.MACHINE_UNAVAILABLE);
            return false;
        }
        lease = allocated.get();
        leasedMachineId = lease.machineId();
        error = ExecutionError.NONE;
        state = JobState.RUNNING;
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
            var moved = route.transfer(Math.min(remaining, needed), false);
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
            var moved = route.transfer(remaining, false);
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
            var moved = route.transfer(Math.min(remaining, buffered), false);
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

    private void beginFinalFlushing() {
        releaseLease();
        stateAfterFlush = JobState.IDLE;
        phase = ExecutionPhase.FLUSHING;
        error = ExecutionError.NONE;
        flushStepBufferInPhase = true;
        state = JobState.RUNNING;
    }

    private void flushStepBuffer(long bandwidth) {
        if (phase != ExecutionPhase.FLUSHING) {
            return;
        }
        var remaining = bandwidth;
        remaining = flushAmounts(pendingFlush, remaining);
        if (flushStepBufferInPhase && remaining > 0L) {
            remaining = flushAmounts(stepBuffer, remaining);
        }

        var done = pendingFlush.isEmpty() && (!flushStepBufferInPhase || stepBuffer.isEmpty());
        if (done) {
            state = stateAfterFlush == null ? JobState.IDLE : stateAfterFlush;
            stateAfterFlush = null;
            if (state == JobState.RUNNING) {
                phase = ExecutionPhase.RUN_STEP;
                if (error == ExecutionError.FLUSH_BACKPRESSURE) {
                    error = ExecutionError.NONE;
                }
            } else {
                phase = ExecutionPhase.TERMINAL;
                error = ExecutionError.NONE;
                clearStepState();
            }
            flushStepBufferInPhase = false;
            return;
        }
        if (remaining > 0L) {
            error = ExecutionError.FLUSH_BACKPRESSURE;
            state = JobState.BLOCKED;
        } else {
            if (error == ExecutionError.FLUSH_BACKPRESSURE) {
                error = ExecutionError.NONE;
            }
            state = JobState.RUNNING;
        }
    }

    private long flushAmounts(Map<IStackKey, Long> amounts, long bandwidth) {
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

    private boolean flushStepBufferNow(Map<IStackKey, Long> amounts) {
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

    private void rollbackReservations(Map<IStackKey, Long> reservations) {
        for (var reserved : reservations.entrySet()) {
            if (reserved.getValue() > 0L) {
                inventory.insert(reserved.getKey(), reserved.getValue(), false);
            }
        }
    }

    private void rollbackBufferedReservations(Map<IStackKey, Long> reservations) {
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

    private void blockStep(ExecutionError code) {
        error = code;
        state = JobState.BLOCKED;
    }

    private void releaseLease() {
        if (lease != null) {
            lease.release();
            lease = null;
        }
        leasedMachineId = null;
    }

    private Map<IStackKey, Long> extractFlushCandidates(CraftStep step) {
        var requiredIntermediateByKey = new HashMap<IStackKey, Long>();
        for (var amount : step.requiredIntermediateOutputs()) {
            requiredIntermediateByKey.merge(amount.key(), amount.amount(), Long::sum);
        }
        var flushCandidates = new HashMap<IStackKey, Long>();
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
            flushCandidates.put(key, flush);
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
