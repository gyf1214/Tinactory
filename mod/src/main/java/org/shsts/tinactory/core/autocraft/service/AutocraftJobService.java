package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ExecutorRuntimeSnapshot;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftJobService implements IAutocraftService {
    private static final long DEFAULT_TRANSMISSION_BANDWIDTH = 64L;
    private static final int DEFAULT_EXECUTION_INTERVAL_TICKS = 1;

    private final Supplier<ICraftExecutor> executorFactory;
    private final long transmissionBandwidth;
    private final int executionIntervalTicks;

    @Nullable
    private AutocraftJob currentJob;
    @Nullable
    private ICraftExecutor runningExecutor;
    private int pendingTicks;

    public AutocraftJobService(
        Supplier<ICraftExecutor> executorFactory,
        long transmissionBandwidth,
        int executionIntervalTicks) {

        this.executorFactory = executorFactory;
        this.transmissionBandwidth = transmissionBandwidth;
        this.executionIntervalTicks = Math.max(1, executionIntervalTicks);
    }

    public AutocraftJobService(
        Supplier<ICraftExecutor> executorFactory) {

        this(executorFactory,
            DEFAULT_TRANSMISSION_BANDWIDTH, DEFAULT_EXECUTION_INTERVAL_TICKS);
    }

    @Override
    public boolean isBusy() {
        return runningExecutor != null;
    }

    public Optional<RunningSnapshot> snapshotRunning() {
        if (currentJob == null || !(runningExecutor instanceof SequentialCraftExecutor sequential)) {
            return Optional.empty();
        }
        return Optional.of(new RunningSnapshot(
            currentJob.id(),
            currentJob.targets(),
            sequential.currentPlan(),
            sequential.snapshot()));
    }

    public void restoreRunning(RunningSnapshot snapshot) {
        if (currentJob != null || runningExecutor != null) {
            return;
        }
        var executor = executorFactory.get();
        if (!(executor instanceof SequentialCraftExecutor sequential)) {
            return;
        }
        sequential.start(snapshot.plan(), snapshot.runtimeSnapshot().nextStepIndex(), snapshot.runtimeSnapshot());
        var details = sequential.details();
        currentJob = new AutocraftJob(
            snapshot.jobId(),
            snapshot.targets(),
            details.blockedReason() == null ? AutocraftJob.Status.RUNNING : AutocraftJob.Status.BLOCKED,
            null,
            sequential.error(),
            details);
        runningExecutor = executor;
        pendingTicks = 0;
    }

    public Optional<CompoundTag> serializeRunningSnapshot(PatternNbtCodec codec) {
        return snapshotRunning().map(snapshot -> serializeSnapshot(snapshot, codec));
    }

    public void restoreRunningSnapshot(CompoundTag tag, PatternNbtCodec codec) {
        restoreRunning(deserializeSnapshot(tag, codec));
    }

    @Override
    public UUID submitPrepared(List<CraftAmount> targets, CraftPlan plan) {
        if (isBusy()) {
            throw new IllegalStateException("autocraft CPU is busy");
        }
        var id = UUID.randomUUID();
        var executor = executorFactory.get();
        executor.start(plan);
        currentJob = new AutocraftJob(
            id,
            targets,
            AutocraftJob.Status.RUNNING,
            null,
            null,
            executor.details());
        runningExecutor = executor;
        pendingTicks = 0;
        return id;
    }

    @Override
    public Optional<AutocraftJob> getJob() {
        return Optional.ofNullable(currentJob);
    }

    @Override
    public Optional<Integer> runningPlanStepCount() {
        return snapshotRunning().map(snapshot -> snapshot.plan().steps().size());
    }

    @Override
    public boolean cancel(UUID id) {
        if (currentJob == null) {
            return false;
        }
        if ((currentJob.status() == AutocraftJob.Status.RUNNING ||
            currentJob.status() == AutocraftJob.Status.BLOCKED) &&
            currentJob.id().equals(id) &&
            runningExecutor != null) {

            runningExecutor.cancel();
            var details = runningExecutor.details();
            currentJob = new AutocraftJob(
                id,
                currentJob.targets(),
                details.blockedReason() == null ? AutocraftJob.Status.RUNNING : AutocraftJob.Status.BLOCKED,
                currentJob.planError(),
                runningExecutor.error(),
                details);
            return true;
        }
        return false;
    }

    public boolean tick() {
        if (currentJob == null || runningExecutor == null) {
            return false;
        }

        pendingTicks++;
        if (pendingTicks < executionIntervalTicks) {
            return false;
        }
        pendingTicks = 0;

        var changed = false;
        runningExecutor.runCycle(transmissionBandwidth);

        var details = runningExecutor.details();
        var state = runningExecutor.state();
        if (state == ExecutionState.RUNNING || state == ExecutionState.IDLE || state == ExecutionState.BLOCKED) {
            var status = details.blockedReason() == null ? AutocraftJob.Status.RUNNING : AutocraftJob.Status.BLOCKED;
            currentJob = new AutocraftJob(
                currentJob.id(),
                currentJob.targets(),
                status,
                currentJob.planError(),
                runningExecutor.error(),
                details);
            return true;
        }

        if (state == ExecutionState.COMPLETED) {
            currentJob = new AutocraftJob(
                currentJob.id(),
                currentJob.targets(),
                AutocraftJob.Status.DONE,
                currentJob.planError(),
                null,
                details);
        } else if (state == ExecutionState.CANCELLED) {
            currentJob = new AutocraftJob(
                currentJob.id(),
                currentJob.targets(),
                AutocraftJob.Status.CANCELLED,
                currentJob.planError(),
                runningExecutor.error(),
                details);
        } else {
            currentJob = new AutocraftJob(
                currentJob.id(),
                currentJob.targets(),
                AutocraftJob.Status.FAILED,
                currentJob.planError(),
                runningExecutor.error(),
                details);
        }
        runningExecutor = null;
        changed = true;
        return changed;
    }

    public record RunningSnapshot(
        UUID jobId,
        List<CraftAmount> targets,
        CraftPlan plan,
        ExecutorRuntimeSnapshot runtimeSnapshot) {}

    private static CompoundTag serializeSnapshot(
        RunningSnapshot snapshot,
        PatternNbtCodec codec) {
        var tag = new CompoundTag();
        tag.putUUID("jobId", snapshot.jobId());
        tag.put("targets", serializeAmounts(snapshot.targets(), codec));
        tag.put("plan", serializePlan(snapshot.plan(), codec));
        tag.put("runtime", serializeRuntime(snapshot.runtimeSnapshot(), codec));
        return tag;
    }

    private static RunningSnapshot deserializeSnapshot(
        CompoundTag tag,
        PatternNbtCodec codec) {
        return new RunningSnapshot(
            tag.getUUID("jobId"),
            deserializeAmounts(tag.getList("targets", TAG_COMPOUND), codec),
            deserializePlan(tag.getCompound("plan"), codec),
            deserializeRuntime(tag.getCompound("runtime"), codec));
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

    private static CraftPlan deserializePlan(
        CompoundTag tag,
        PatternNbtCodec codec) {
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

    private static CompoundTag serializeRuntime(ExecutorRuntimeSnapshot snapshot, PatternNbtCodec codec) {
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

    private static ExecutorRuntimeSnapshot deserializeRuntime(CompoundTag tag, PatternNbtCodec codec) {
        return new ExecutorRuntimeSnapshot(
            ExecutionState.valueOf(tag.getString("state")),
            ExecutionDetails.Phase.valueOf(tag.getString("phase")),
            tag.contains("error", TAG_COMPOUND) ? deserializeError(tag.getCompound("error")) : null,
            tag.contains("blockedReason") ? ExecutionError.Code.valueOf(tag.getString("blockedReason")) : null,
            tag.contains("pendingTerminalState") ? ExecutionState.valueOf(tag.getString("pendingTerminalState")) : null,
            tag.getInt("nextStepIndex"),
            deserializeKeyedAmounts(tag.getList("stepBuffer", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("stepProducedOutputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("stepRequiredOutputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("stepRequiredInputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("transmittedInputs", TAG_COMPOUND), codec),
            deserializeKeyedAmounts(tag.getList("transmittedRequiredOutputs", TAG_COMPOUND), codec),
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

    private static ListTag serializeKeyedAmounts(Map<IIngredientKey, Long> amounts, PatternNbtCodec codec) {
        var out = new ListTag();
        for (var entry : amounts.entrySet()) {
            out.add(codec.encodeAmount(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    private static Map<IIngredientKey, Long> deserializeKeyedAmounts(ListTag tags, PatternNbtCodec codec) {
        var out = new LinkedHashMap<IIngredientKey, Long>();
        for (var i = 0; i < tags.size(); i++) {
            var amount = codec.decodeAmount(tags.getCompound(i));
            out.put(amount.key(), amount.amount());
        }
        return out;
    }
}
