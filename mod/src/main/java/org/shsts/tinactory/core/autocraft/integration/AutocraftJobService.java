package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ExecutorRuntimeSnapshot;
import org.shsts.tinactory.core.autocraft.exec.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftJobService {
    private static final long DEFAULT_TRANSMISSION_BANDWIDTH = 64L;
    private static final int DEFAULT_EXECUTION_INTERVAL_TICKS = 1;

    private final UUID cpuId;
    private final ICraftPlanner planner;
    private final Supplier<ICraftExecutor> executorFactory;
    private final Supplier<List<CraftAmount>> availableSupplier;
    private final long transmissionBandwidth;
    private final int executionIntervalTicks;

    private final Map<UUID, AutocraftJob> jobs = new LinkedHashMap<>();
    private final Map<UUID, CraftPlan> preparedPlans = new LinkedHashMap<>();
    private final Queue<UUID> queued = new ArrayDeque<>();

    @Nullable
    private UUID runningJobId;
    @Nullable
    private ICraftExecutor runningExecutor;
    private int pendingTicks;

    public AutocraftJobService(
        UUID cpuId,
        ICraftPlanner planner,
        Supplier<ICraftExecutor> executorFactory,
        Supplier<List<CraftAmount>> availableSupplier,
        long transmissionBandwidth,
        int executionIntervalTicks) {

        this.cpuId = cpuId;
        this.planner = planner;
        this.executorFactory = executorFactory;
        this.availableSupplier = availableSupplier;
        this.transmissionBandwidth = transmissionBandwidth;
        this.executionIntervalTicks = Math.max(1, executionIntervalTicks);
    }

    public AutocraftJobService(
        UUID cpuId,
        ICraftPlanner planner,
        Supplier<ICraftExecutor> executorFactory,
        Supplier<List<CraftAmount>> availableSupplier) {

        this(cpuId, planner, executorFactory, availableSupplier,
            DEFAULT_TRANSMISSION_BANDWIDTH, DEFAULT_EXECUTION_INTERVAL_TICKS);
    }

    public AutocraftJobService(
        ICraftPlanner planner,
        Supplier<ICraftExecutor> executorFactory,
        Supplier<List<CraftAmount>> availableSupplier) {

        this(UUID.randomUUID(), planner, executorFactory, availableSupplier,
            DEFAULT_TRANSMISSION_BANDWIDTH, DEFAULT_EXECUTION_INTERVAL_TICKS);
    }

    public UUID cpuId() {
        return cpuId;
    }

    public boolean isBusy() {
        return runningJobId != null || !queued.isEmpty();
    }

    public Optional<RunningSnapshot> snapshotRunning() {
        if (runningJobId == null || !(runningExecutor instanceof SequentialCraftExecutor sequential)) {
            return Optional.empty();
        }
        var current = jobs.get(runningJobId);
        if (current == null) {
            return Optional.empty();
        }
        return Optional.of(new RunningSnapshot(
            cpuId,
            current.id(),
            current.targets(),
            sequential.currentPlan(),
            sequential.snapshot()));
    }

    public void restoreRunning(RunningSnapshot snapshot) {
        if (!snapshot.cpuId().equals(cpuId) || runningJobId != null || !queued.isEmpty()) {
            return;
        }
        var executor = executorFactory.get();
        if (!(executor instanceof SequentialCraftExecutor sequential)) {
            return;
        }
        sequential.start(snapshot.plan(), snapshot.runtimeSnapshot().nextStepIndex(), snapshot.runtimeSnapshot());
        var details = sequential.details();
        jobs.put(snapshot.jobId(), new AutocraftJob(
            snapshot.jobId(),
            snapshot.targets(),
            details.blockedReason() == null ? AutocraftJob.Status.RUNNING : AutocraftJob.Status.BLOCKED,
            null,
            sequential.error(),
            details));
        runningJobId = snapshot.jobId();
        runningExecutor = executor;
        pendingTicks = 0;
    }

    public Optional<CompoundTag> serializeRunningSnapshot(PatternNbtCodec codec) {
        return snapshotRunning().map(snapshot -> serializeSnapshot(snapshot, codec));
    }

    public void restoreRunningSnapshot(CompoundTag tag, PatternNbtCodec codec) {
        restoreRunning(deserializeSnapshot(tag, codec));
    }

    public UUID submit(List<CraftAmount> targets) {
        if (isBusy()) {
            throw new IllegalStateException("autocraft CPU is busy");
        }
        var id = UUID.randomUUID();
        jobs.put(id, new AutocraftJob(id, targets, AutocraftJob.Status.QUEUED, null, null, null));
        queued.add(id);
        return id;
    }

    public UUID submitPrepared(List<CraftAmount> targets, CraftPlan plan) {
        if (isBusy()) {
            throw new IllegalStateException("autocraft CPU is busy");
        }
        var id = UUID.randomUUID();
        jobs.put(id, new AutocraftJob(id, targets, AutocraftJob.Status.QUEUED, null, null, null));
        preparedPlans.put(id, plan);
        queued.add(id);
        return id;
    }

    public AutocraftJob job(UUID id) {
        return jobs.get(id);
    }

    public Optional<AutocraftJob> findJob(UUID id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public List<AutocraftJob> listJobs() {
        return List.copyOf(jobs.values());
    }

    public boolean cancel(UUID id) {
        var current = jobs.get(id);
        if (current == null) {
            return false;
        }
        if (current.status() == AutocraftJob.Status.QUEUED) {
            queued.remove(id);
            preparedPlans.remove(id);
            jobs.put(id, new AutocraftJob(
                id,
                current.targets(),
                AutocraftJob.Status.CANCELLED,
                current.planError(),
                null,
                current.executionDetails()));
            return true;
        }
        if ((current.status() == AutocraftJob.Status.RUNNING || current.status() == AutocraftJob.Status.BLOCKED) &&
            id.equals(runningJobId)) {

            if (runningExecutor != null) {
                runningExecutor.cancel();
                var details = runningExecutor.details();
                jobs.put(id, new AutocraftJob(
                    id,
                    current.targets(),
                    details.blockedReason() == null ? AutocraftJob.Status.RUNNING : AutocraftJob.Status.BLOCKED,
                    current.planError(),
                    runningExecutor.error(),
                    details));
            }
            return true;
        }
        return false;
    }

    public boolean tick() {
        if (runningJobId == null) {
            return startNextJob();
        }
        if (runningExecutor == null) {
            return false;
        }

        pendingTicks++;
        if (pendingTicks < executionIntervalTicks) {
            return false;
        }
        pendingTicks = 0;

        var changed = false;
        runningExecutor.runCycle(transmissionBandwidth);
        var current = jobs.get(runningJobId);
        if (current == null) {
            runningJobId = null;
            runningExecutor = null;
            return true;
        }

        var details = runningExecutor.details();
        var state = runningExecutor.state();
        if (state == ExecutionState.RUNNING || state == ExecutionState.IDLE || state == ExecutionState.BLOCKED) {
            var status = details.blockedReason() == null ? AutocraftJob.Status.RUNNING : AutocraftJob.Status.BLOCKED;
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                status,
                current.planError(),
                runningExecutor.error(),
                details));
            return true;
        }

        if (state == ExecutionState.COMPLETED) {
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                AutocraftJob.Status.DONE,
                current.planError(),
                null,
                details));
        } else if (state == ExecutionState.CANCELLED) {
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                AutocraftJob.Status.CANCELLED,
                current.planError(),
                runningExecutor.error(),
                details));
        } else {
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                AutocraftJob.Status.FAILED,
                current.planError(),
                runningExecutor.error(),
                details));
        }
        runningJobId = null;
        runningExecutor = null;
        changed = true;
        return changed;
    }

    private boolean startNextJob() {
        var id = queued.poll();
        if (id == null) {
            return false;
        }
        var current = jobs.get(id);
        if (current == null) {
            return false;
        }

        var prepared = preparedPlans.remove(id);
        var plan = prepared;
        if (plan == null) {
            var result = planner.plan(current.targets(), availableSupplier.get());
            if (!result.isSuccess()) {
                jobs.put(id, new AutocraftJob(
                    id,
                    current.targets(),
                    AutocraftJob.Status.FAILED,
                    result.error(),
                    null,
                    null));
                return true;
            }
            plan = result.plan();
        }

        var executor = executorFactory.get();
        executor.start(plan);
        jobs.put(id, new AutocraftJob(
            id,
            current.targets(),
            AutocraftJob.Status.RUNNING,
            null,
            null,
            executor.details()));

        runningJobId = id;
        runningExecutor = executor;
        pendingTicks = 0;
        return true;
    }

    public record RunningSnapshot(
        UUID cpuId,
        UUID jobId,
        List<CraftAmount> targets,
        CraftPlan plan,
        ExecutorRuntimeSnapshot runtimeSnapshot) {}

    private static CompoundTag serializeSnapshot(RunningSnapshot snapshot, PatternNbtCodec codec) {
        var tag = new CompoundTag();
        tag.putUUID("cpuId", snapshot.cpuId());
        tag.putUUID("jobId", snapshot.jobId());
        tag.put("targets", serializeAmounts(snapshot.targets()));
        tag.put("plan", serializePlan(snapshot.plan(), codec));
        tag.put("runtime", serializeRuntime(snapshot.runtimeSnapshot()));
        return tag;
    }

    private static RunningSnapshot deserializeSnapshot(CompoundTag tag, PatternNbtCodec codec) {
        return new RunningSnapshot(
            tag.getUUID("cpuId"),
            tag.getUUID("jobId"),
            deserializeAmounts(tag.getList("targets", TAG_COMPOUND)),
            deserializePlan(tag.getCompound("plan"), codec),
            deserializeRuntime(tag.getCompound("runtime")));
    }

    private static ListTag serializeAmounts(List<CraftAmount> amounts) {
        var out = new ListTag();
        for (var amount : amounts) {
            out.add(serializeAmount(amount));
        }
        return out;
    }

    private static List<CraftAmount> deserializeAmounts(ListTag amounts) {
        var out = new ArrayList<CraftAmount>(amounts.size());
        for (var i = 0; i < amounts.size(); i++) {
            out.add(deserializeAmount(amounts.getCompound(i)));
        }
        return out;
    }

    private static CompoundTag serializeAmount(CraftAmount amount) {
        var tag = new CompoundTag();
        tag.putString("type", amount.key().type().name());
        tag.putString("id", amount.key().id());
        tag.putString("nbt", amount.key().nbt());
        tag.putLong("amount", amount.amount());
        return tag;
    }

    private static CraftAmount deserializeAmount(CompoundTag tag) {
        var type = CraftKey.Type.valueOf(tag.getString("type"));
        CraftKey key;
        if (type == CraftKey.Type.FLUID) {
            key = CraftKey.fluid(tag.getString("id"), tag.getString("nbt"));
        } else {
            key = CraftKey.item(tag.getString("id"), tag.getString("nbt"));
        }
        return new CraftAmount(key, tag.getLong("amount"));
    }

    private static CompoundTag serializePlan(CraftPlan plan, PatternNbtCodec codec) {
        var tag = new CompoundTag();
        var steps = new ListTag();
        for (var step : plan.steps()) {
            var stepTag = new CompoundTag();
            stepTag.putString("stepId", step.stepId());
            stepTag.putLong("runs", step.runs());
            stepTag.put("pattern", codec.encodePattern(step.pattern()));
            stepTag.put("requiredIntermediateOutputs", serializeAmounts(step.requiredIntermediateOutputs()));
            stepTag.put("requiredFinalOutputs", serializeAmounts(step.requiredFinalOutputs()));
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
                deserializeAmounts(stepTag.getList("requiredIntermediateOutputs", TAG_COMPOUND)),
                deserializeAmounts(stepTag.getList("requiredFinalOutputs", TAG_COMPOUND))));
        }
        return new CraftPlan(out);
    }

    private static CompoundTag serializeRuntime(ExecutorRuntimeSnapshot snapshot) {
        var tag = new CompoundTag();
        tag.putString("state", snapshot.state().name());
        tag.putString("phase", snapshot.phase().name());
        if (snapshot.error() != null) {
            tag.put("error", serializeError(snapshot.error()));
        }
        if (snapshot.blockedReason() != null) {
            tag.putString("blockedReason", snapshot.blockedReason().name());
        }
        if (snapshot.pendingTerminalState() != null) {
            tag.putString("pendingTerminalState", snapshot.pendingTerminalState().name());
        }
        tag.putInt("nextStepIndex", snapshot.nextStepIndex());
        tag.put("stepBuffer", serializeKeyedAmounts(snapshot.stepBuffer()));
        tag.put("stepProducedOutputs", serializeKeyedAmounts(snapshot.stepProducedOutputs()));
        tag.put("stepRequiredOutputs", serializeKeyedAmounts(snapshot.stepRequiredOutputs()));
        tag.put("stepRequiredInputs", serializeKeyedAmounts(snapshot.stepRequiredInputs()));
        tag.put("transmittedInputs", serializeKeyedAmounts(snapshot.transmittedInputs()));
        tag.put("transmittedRequiredOutputs", serializeKeyedAmounts(snapshot.transmittedRequiredOutputs()));
        if (snapshot.leasedMachineId() != null) {
            tag.putUUID("leasedMachineId", snapshot.leasedMachineId());
        }
        return tag;
    }

    private static ExecutorRuntimeSnapshot deserializeRuntime(CompoundTag tag) {
        return new ExecutorRuntimeSnapshot(
            ExecutionState.valueOf(tag.getString("state")),
            ExecutionDetails.Phase.valueOf(tag.getString("phase")),
            tag.contains("error", TAG_COMPOUND) ? deserializeError(tag.getCompound("error")) : null,
            tag.contains("blockedReason") ? ExecutionError.Code.valueOf(tag.getString("blockedReason")) : null,
            tag.contains("pendingTerminalState") ? ExecutionState.valueOf(tag.getString("pendingTerminalState")) : null,
            tag.getInt("nextStepIndex"),
            deserializeKeyedAmounts(tag.getList("stepBuffer", TAG_COMPOUND)),
            deserializeKeyedAmounts(tag.getList("stepProducedOutputs", TAG_COMPOUND)),
            deserializeKeyedAmounts(tag.getList("stepRequiredOutputs", TAG_COMPOUND)),
            deserializeKeyedAmounts(tag.getList("stepRequiredInputs", TAG_COMPOUND)),
            deserializeKeyedAmounts(tag.getList("transmittedInputs", TAG_COMPOUND)),
            deserializeKeyedAmounts(tag.getList("transmittedRequiredOutputs", TAG_COMPOUND)),
            tag.hasUUID("leasedMachineId") ? tag.getUUID("leasedMachineId") : null);
    }

    private static CompoundTag serializeError(ExecutionError error) {
        var tag = new CompoundTag();
        tag.putString("code", error.code().name());
        tag.putString("stepId", error.stepId());
        tag.putString("message", error.message());
        return tag;
    }

    private static ExecutionError deserializeError(CompoundTag tag) {
        return new ExecutionError(
            ExecutionError.Code.valueOf(tag.getString("code")),
            tag.getString("stepId"),
            tag.getString("message"));
    }

    private static ListTag serializeKeyedAmounts(Map<CraftKey, Long> amounts) {
        var out = new ListTag();
        for (var entry : amounts.entrySet()) {
            var tag = new CompoundTag();
            tag.putString("type", entry.getKey().type().name());
            tag.putString("id", entry.getKey().id());
            tag.putString("nbt", entry.getKey().nbt());
            tag.putLong("amount", entry.getValue());
            out.add(tag);
        }
        return out;
    }

    private static Map<CraftKey, Long> deserializeKeyedAmounts(ListTag tags) {
        var out = new LinkedHashMap<CraftKey, Long>();
        for (var i = 0; i < tags.size(); i++) {
            var tag = tags.getCompound(i);
            var type = CraftKey.Type.valueOf(tag.getString("type"));
            CraftKey key;
            if (type == CraftKey.Type.FLUID) {
                key = CraftKey.fluid(tag.getString("id"), tag.getString("nbt"));
            } else {
                key = CraftKey.item(tag.getString("id"), tag.getString("nbt"));
            }
            out.put(key, tag.getLong("amount"));
        }
        return out;
    }
}
