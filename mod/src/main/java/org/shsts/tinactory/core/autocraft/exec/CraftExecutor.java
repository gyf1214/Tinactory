package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.nbt.Tag.TAG_LIST;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CraftExecutor implements ICraftExecutor {
    private final IInventoryView inventory;
    private final IMachineAllocator machineAllocator;
    private final IJobEvents jobEvents;
    private final int activeRuntimeLimit;

    private CraftPlan plan = new CraftPlan(List.of());
    private JobState state = JobState.IDLE;
    private ExecutionPhase phase = ExecutionPhase.TERMINAL;
    private ExecutionError error = ExecutionError.NONE;
    @Nullable
    private JobState stateAfterFlush;
    private int nextUnscheduledStepIndex;
    private final Map<IStackKey, Long> sharedBuffer = new HashMap<>();
    private final Map<IStackKey, Long> requiredInventory = new LinkedHashMap<>();
    private final List<StepRuntime> activeRuntimes = new ArrayList<>();

    public CraftExecutor(IInventoryView inventory, IMachineAllocator machineAllocator, IJobEvents jobEvents) {
        this(inventory, machineAllocator, jobEvents, 1);
    }

    public CraftExecutor(
        IInventoryView inventory,
        IMachineAllocator machineAllocator,
        IJobEvents jobEvents,
        int activeRuntimeLimit) {

        this.inventory = inventory;
        this.machineAllocator = machineAllocator;
        this.jobEvents = jobEvents;
        this.activeRuntimeLimit = Math.max(1, activeRuntimeLimit);
    }

    @Override
    public void start(CraftPlan plan) {
        ensureNotActive("start");
        initializeRun(plan, 0);
    }

    public void restore(ExecutorSnapshot snapshot) {
        ensureNotActive("restore");
        initializeRun(snapshot.plan(), snapshot.nextUnscheduledStepIndex());
        restoreSnapshot(snapshot);
    }

    @Override
    public void restore(CompoundTag tag, PatternCodec codec) {
        restore(deserialize(tag, codec));
    }

    @Override
    public void runCycle(long itemBandwidth, long fluidBandwidth) {
        if (phase == ExecutionPhase.TERMINAL || state == JobState.FAILED) {
            return;
        }
        if (phase == ExecutionPhase.FLUSHING) {
            flushSharedBuffer(itemBandwidth, fluidBandwidth);
            return;
        }
        if (phase == ExecutionPhase.RESERVING) {
            reserveRequiredInventory(itemBandwidth, fluidBandwidth);
            if (!hasReservedRequiredInventory()) {
                return;
            }
            phase = ExecutionPhase.RUN_STEP;
        }
        rebindOrReassignRuntimes();
        scheduleRuntimes();
        runActiveRuntimes(itemBandwidth, fluidBandwidth);
        completeRuntimes();
        if (nextUnscheduledStepIndex >= plan.steps().size() && activeRuntimes.isEmpty()) {
            beginFinalFlushing();
            flushSharedBuffer(itemBandwidth, fluidBandwidth);
            return;
        }
        updateAggregateState();
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
        return nextUnscheduledStepIndex - activeRuntimes.size();
    }

    @Override
    public int totalSteps() {
        return plan.steps().size();
    }

    @Override
    public CompoundTag serialize(PatternCodec codec) {
        return serialize(snapshot(), codec);
    }

    public ExecutorSnapshot snapshot() {
        return new ExecutorSnapshot(
            state,
            phase,
            error,
            stateAfterFlush,
            plan,
            requiredInventory,
            sharedBuffer,
            nextUnscheduledStepIndex,
            activeRuntimes.stream().map(StepRuntime::snapshot).toList());
    }

    private void initializeRun(CraftPlan plan, int nextStepIndex) {
        this.plan = plan;
        state = plan.steps().isEmpty() || nextStepIndex >= plan.steps().size() ? JobState.IDLE : JobState.RUNNING;
        phase = state == JobState.RUNNING ? ExecutionPhase.RESERVING : ExecutionPhase.TERMINAL;
        error = ExecutionError.NONE;
        stateAfterFlush = null;
        nextUnscheduledStepIndex = nextStepIndex;
        releaseAllLeases();
        activeRuntimes.clear();
        sharedBuffer.clear();
        requiredInventory.clear();
        requiredInventory.putAll(inventoryRequirements(plan));
    }

    private void restoreSnapshot(ExecutorSnapshot snapshot) {
        state = snapshot.state();
        phase = snapshot.phase();
        error = snapshot.error();
        stateAfterFlush = snapshot.stateAfterFlush();
        nextUnscheduledStepIndex = snapshot.nextUnscheduledStepIndex();
        sharedBuffer.clear();
        sharedBuffer.putAll(snapshot.sharedBuffer());
        requiredInventory.clear();
        requiredInventory.putAll(snapshot.requiredInventory());
        activeRuntimes.clear();
        for (var runtime : snapshot.activeRuntimes()) {
            activeRuntimes.add(new StepRuntime(
                plan.steps().get(runtime.stepIndex()),
                runtime));
        }
        activeRuntimes.sort(Comparator.comparingInt(StepRuntime::stepIndex));
    }

    private Map<IStackKey, Long> inventoryRequirements(CraftPlan plan) {
        var out = new LinkedHashMap<IStackKey, Long>();
        for (var entry : plan.summary().entries().entrySet()) {
            if (entry.getValue().consumedFromInventory() > 0L) {
                out.put(entry.getKey(), entry.getValue().consumedFromInventory());
            }
        }
        if (!out.isEmpty() || !plan.summary().entries().isEmpty()) {
            return out;
        }
        var produced = new HashMap<IStackKey, Long>();
        for (var step : plan.steps()) {
            for (var input : step.pattern().inputs()) {
                var needed = input.amount() * step.runs();
                var available = produced.getOrDefault(input.key(), 0L);
                var consumedProduced = Math.min(available, needed);
                if (consumedProduced > 0L) {
                    produced.put(input.key(), available - consumedProduced);
                    needed -= consumedProduced;
                }
                if (needed > 0L) {
                    out.merge(input.key(), needed, Long::sum);
                }
            }
            for (var output : step.pattern().outputs()) {
                produced.merge(output.key(), output.amount() * step.runs(), Long::sum);
            }
        }
        return out;
    }

    private void reserveRequiredInventory(long itemBandwidth, long fluidBandwidth) {
        var movedItems = reserveRequiredInventory(PortType.ITEM, itemBandwidth);
        var movedFluids = reserveRequiredInventory(PortType.FLUID, fluidBandwidth);
        if (hasReservedRequiredInventory()) {
            error = ExecutionError.NONE;
            state = JobState.RUNNING;
            return;
        }
        if (movedItems <= 0L && movedFluids <= 0L) {
            block(ExecutionError.INPUT_UNAVAILABLE);
        } else {
            error = ExecutionError.NONE;
            state = JobState.RUNNING;
        }
    }

    private long reserveRequiredInventory(PortType type, long bandwidth) {
        var remaining = bandwidth;
        var movedTotal = 0L;
        for (var required : requiredInventory.entrySet()) {
            if (remaining <= 0L) {
                break;
            }
            var key = required.getKey();
            if (key.type() != type) {
                continue;
            }
            var missing = required.getValue() - sharedBuffer.getOrDefault(key, 0L);
            if (missing <= 0L) {
                continue;
            }
            var moved = inventory.extract(key, Math.min(missing, remaining), false);
            if (moved <= 0L) {
                continue;
            }
            sharedBuffer.merge(key, moved, Long::sum);
            remaining -= moved;
            movedTotal += moved;
        }
        return movedTotal;
    }

    private boolean hasReservedRequiredInventory() {
        for (var required : requiredInventory.entrySet()) {
            if (sharedBuffer.getOrDefault(required.getKey(), 0L) < required.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void rebindOrReassignRuntimes() {
        var excluded = leasedMachineIds();
        for (var runtime : activeRuntimes) {
            if (runtime.hasValidLease()) {
                continue;
            }
            runtime.releaseLease();
            var machineId = runtime.leasedMachineId();
            if (machineId != null) {
                var exact = machineAllocator.allocate(runtime.step(), machineId);
                if (exact.isPresent() && exact.get().isValid()) {
                    runtime.bindLease(exact.get());
                    excluded.add(exact.get().machineId());
                    continue;
                } else if (exact.isPresent()) {
                    exact.get().release();
                }
            }
            if (!runtime.canReassignAfterMachineLoss()) {
                runtime.block(ExecutionError.MACHINE_UNAVAILABLE);
                continue;
            }
            var replacement = machineAllocator.allocate(runtime.step(), excluded);
            if (replacement.isPresent() && replacement.get().isValid()) {
                runtime.bindLease(replacement.get());
                excluded.add(replacement.get().machineId());
            } else if (replacement.isPresent()) {
                replacement.get().release();
                runtime.block(ExecutionError.MACHINE_UNAVAILABLE);
            } else {
                runtime.block(ExecutionError.MACHINE_UNAVAILABLE);
            }
        }
    }

    private void scheduleRuntimes() {
        var excluded = leasedMachineIds();
        while (activeRuntimes.size() < activeRuntimeLimit && nextUnscheduledStepIndex < plan.steps().size()) {
            var step = plan.steps().get(nextUnscheduledStepIndex);
            var allocated = machineAllocator.allocate(step, excluded);
            if (allocated.isEmpty()) {
                block(ExecutionError.MACHINE_UNAVAILABLE);
                return;
            }
            var runtime = new StepRuntime(nextUnscheduledStepIndex, step, allocated.get());
            runtime.markStarted();
            jobEvents.onStepStarted(step);
            activeRuntimes.add(runtime);
            excluded.add(allocated.get().machineId());
            nextUnscheduledStepIndex++;
            if (!runtime.hasValidLease()) {
                rebindOrReassignRuntimes();
                return;
            }
            error = ExecutionError.NONE;
            state = JobState.RUNNING;
        }
    }

    private void runActiveRuntimes(long itemBandwidth, long fluidBandwidth) {
        for (var runtime : activeRuntimes) {
            if (!runtime.hasValidLease()) {
                continue;
            }
            runtime.transfer(PortType.ITEM, itemBandwidth, sharedBuffer);
            runtime.transfer(PortType.FLUID, fluidBandwidth, sharedBuffer);
        }
    }

    private void completeRuntimes() {
        for (var runtime : List.copyOf(activeRuntimes)) {
            if (!runtime.completed()) {
                continue;
            }
            runtime.releaseLease();
            activeRuntimes.remove(runtime);
            jobEvents.onStepCompleted(runtime.step());
            error = ExecutionError.NONE;
            state = JobState.RUNNING;
        }
    }

    private void updateAggregateState() {
        for (var runtime : activeRuntimes) {
            if (runtime.blockReason() == null) {
                error = ExecutionError.NONE;
                state = JobState.RUNNING;
                return;
            }
        }
        if (!activeRuntimes.isEmpty()) {
            var reason = activeRuntimes.getFirst().blockReason();
            block(reason == null ? ExecutionError.NONE : reason);
        }
    }

    private Set<UUID> leasedMachineIds() {
        var out = new HashSet<UUID>();
        for (var runtime : activeRuntimes) {
            var machineId = runtime.leasedMachineId();
            if (runtime.lease() != null && machineId != null) {
                out.add(machineId);
            }
        }
        return out;
    }

    private void beginFinalFlushing() {
        releaseAllLeases();
        stateAfterFlush = JobState.IDLE;
        phase = ExecutionPhase.FLUSHING;
        error = ExecutionError.NONE;
        state = JobState.RUNNING;
    }

    private void flushSharedBuffer(long itemBandwidth, long fluidBandwidth) {
        var remainingItems = flushAmounts(PortType.ITEM, itemBandwidth);
        var remainingFluids = flushAmounts(PortType.FLUID, fluidBandwidth);
        if (sharedBuffer.isEmpty()) {
            state = stateAfterFlush == null ? JobState.IDLE : stateAfterFlush;
            stateAfterFlush = null;
            phase = state == JobState.RUNNING ? ExecutionPhase.RUN_STEP : ExecutionPhase.TERMINAL;
            error = ExecutionError.NONE;
            return;
        }
        if (remainingItems > 0L || remainingFluids > 0L) {
            block(ExecutionError.FLUSH_BLOCKED);
        } else {
            error = ExecutionError.NONE;
            state = JobState.RUNNING;
        }
    }

    private long flushAmounts(PortType type, long bandwidth) {
        var remaining = bandwidth;
        for (var entry : List.copyOf(sharedBuffer.entrySet())) {
            if (remaining <= 0L) {
                break;
            }
            if (entry.getKey().type() != type) {
                continue;
            }
            var inserted = inventory.insert(entry.getKey(), Math.min(remaining, entry.getValue()), false);
            if (inserted <= 0L) {
                continue;
            }
            var left = entry.getValue() - inserted;
            if (left <= 0L) {
                sharedBuffer.remove(entry.getKey());
            } else {
                sharedBuffer.put(entry.getKey(), left);
            }
            remaining -= inserted;
        }
        return remaining;
    }

    private void releaseAllLeases() {
        for (var runtime : activeRuntimes) {
            runtime.releaseLease();
        }
    }

    private void block(ExecutionError code) {
        error = code;
        state = JobState.BLOCKED;
    }

    private void ensureNotActive(String action) {
        if (state == JobState.RUNNING || state == JobState.BLOCKED) {
            throw new IllegalStateException("cannot " + action + " active executor");
        }
    }

    private static CompoundTag serialize(ExecutorSnapshot snapshot, PatternCodec codec) {
        var tag = new CompoundTag();
        tag.putString("state", snapshot.state().name());
        tag.putString("phase", snapshot.phase().name());
        tag.put("error", serializeError(snapshot.error()));
        if (snapshot.stateAfterFlush() != null) {
            tag.putString("stateAfterFlush", snapshot.stateAfterFlush().name());
        }
        tag.put("plan", serializePlan(snapshot.plan(), codec));
        tag.put("requiredInventory", serializeKeyedAmounts(snapshot.requiredInventory(), codec));
        tag.put("sharedBuffer", serializeKeyedAmounts(snapshot.sharedBuffer(), codec));
        tag.putInt("nextUnscheduledStepIndex", snapshot.nextUnscheduledStepIndex());
        var runtimes = new ListTag();
        for (var runtime : snapshot.activeRuntimes()) {
            var runtimeTag = new CompoundTag();
            runtimeTag.putInt("stepIndex", runtime.stepIndex());
            if (runtime.leasedMachineId() != null) {
                runtimeTag.putUUID("leasedMachineId", runtime.leasedMachineId());
            }
            runtimeTag.put("requiredInputs", serializeKeyedAmounts(runtime.requiredInputs(), codec));
            runtimeTag.put("requiredOutputs", serializeKeyedAmounts(runtime.requiredOutputs(), codec));
            runtimeTag.put("producedOutputs", serializeKeyedAmounts(runtime.producedOutputs(), codec));
            runtimeTag.put("transmittedInputs", serializeKeyedAmounts(runtime.transmittedInputs(), codec));
            runtimeTag.put(
                "transmittedRequiredOutputs",
                serializeKeyedAmounts(runtime.transmittedRequiredOutputs(), codec));
            if (runtime.blockReason() != null) {
                runtimeTag.put("blockReason", serializeError(runtime.blockReason()));
            }
            runtimeTag.putBoolean("started", runtime.started());
            runtimes.add(runtimeTag);
        }
        tag.put("activeRuntimes", runtimes);
        return tag;
    }

    private static ExecutorSnapshot deserialize(CompoundTag tag, PatternCodec codec) {
        var plan = deserializePlan(tag.getCompound("plan"), codec);
        var activeRuntimeTags = tag.getList("activeRuntimes", TAG_COMPOUND);
        var activeRuntimes = new ArrayList<StepRuntime.Snapshot>();
        for (var i = 0; i < activeRuntimeTags.size(); i++) {
            var runtimeTag = activeRuntimeTags.getCompound(i);
            activeRuntimes.add(new StepRuntime.Snapshot(
                runtimeTag.getInt("stepIndex"),
                runtimeTag.hasUUID("leasedMachineId") ? runtimeTag.getUUID("leasedMachineId") : null,
                deserializeKeyedAmounts(runtimeTag.getList("requiredInputs", TAG_COMPOUND), codec),
                deserializeKeyedAmounts(runtimeTag.getList("requiredOutputs", TAG_COMPOUND), codec),
                deserializeKeyedAmounts(runtimeTag.getList("producedOutputs", TAG_COMPOUND), codec),
                deserializeKeyedAmounts(runtimeTag.getList("transmittedInputs", TAG_COMPOUND), codec),
                deserializeKeyedAmounts(runtimeTag.getList("transmittedRequiredOutputs", TAG_COMPOUND), codec),
                runtimeTag.contains("blockReason", TAG_COMPOUND) ?
                    deserializeError(runtimeTag.getCompound("blockReason")) :
                    null,
                runtimeTag.getBoolean("started")));
        }
        return new ExecutorSnapshot(
            JobState.valueOf(tag.getString("state")),
            ExecutionPhase.valueOf(tag.getString("phase")),
            tag.contains("error", TAG_COMPOUND) ? deserializeError(tag.getCompound("error")) : ExecutionError.NONE,
            deserializeStateAfterFlush(tag),
            plan,
            tag.contains("requiredInventory", TAG_LIST) ?
                deserializeKeyedAmounts(tag.getList("requiredInventory", TAG_COMPOUND), codec) :
                Map.of(),
            tag.contains("sharedBuffer", TAG_LIST) ?
                deserializeKeyedAmounts(tag.getList("sharedBuffer", TAG_COMPOUND), codec) :
                Map.of(),
            tag.getInt("nextUnscheduledStepIndex"),
            activeRuntimes);
    }

    @Nullable
    private static JobState deserializeStateAfterFlush(CompoundTag tag) {
        return tag.contains("stateAfterFlush") ? JobState.valueOf(tag.getString("stateAfterFlush")) : null;
    }

    private static CompoundTag serializePlan(CraftPlan plan, PatternCodec codec) {
        var tag = new CompoundTag();
        var steps = new ListTag();
        for (var step : plan.steps()) {
            var stepTag = new CompoundTag();
            stepTag.putString("stepId", step.stepId());
            stepTag.putLong("runs", step.runs());
            stepTag.put("pattern", codec.encodePattern(step.pattern()));
            steps.add(stepTag);
        }
        tag.put("steps", steps);
        return tag;
    }

    private static CraftPlan deserializePlan(CompoundTag tag, PatternCodec codec) {
        var steps = tag.getList("steps", TAG_COMPOUND);
        var out = new ArrayList<CraftStep>(steps.size());
        for (var i = 0; i < steps.size(); i++) {
            var stepTag = steps.getCompound(i);
            out.add(new CraftStep(
                stepTag.getString("stepId"),
                codec.decodePattern(stepTag.getCompound("pattern")),
                stepTag.getLong("runs")));
        }
        return new CraftPlan(out);
    }

    private static CompoundTag serializeError(ExecutionError error) {
        var tag = new CompoundTag();
        tag.putString("value", error.name());
        return tag;
    }

    private static ExecutionError deserializeError(CompoundTag tag) {
        return ExecutionError.valueOf(tag.getString("value"));
    }

    private static ListTag serializeKeyedAmounts(Map<IStackKey, Long> amounts, PatternCodec codec) {
        var out = new ListTag();
        for (var entry : amounts.entrySet()) {
            out.add(codec.encodeAmount(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    private static Map<IStackKey, Long> deserializeKeyedAmounts(ListTag tags, PatternCodec codec) {
        var out = new LinkedHashMap<IStackKey, Long>();
        for (var i = 0; i < tags.size(); i++) {
            var amount = codec.decodeAmount(tags.getCompound(i));
            out.put(amount.key(), amount.amount());
        }
        return out;
    }
}
