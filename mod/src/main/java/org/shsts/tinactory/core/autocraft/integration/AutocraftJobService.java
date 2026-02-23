package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
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
    private final UUID cpuId;
    private final ICraftPlanner planner;
    private final Supplier<ICraftExecutor> executorFactory;
    private final Supplier<List<CraftAmount>> availableSupplier;

    private final Map<UUID, AutocraftJob> jobs = new LinkedHashMap<>();
    private final Queue<UUID> queued = new ArrayDeque<>();

    @Nullable
    private UUID runningJobId;
    @Nullable
    private ICraftExecutor runningExecutor;

    public AutocraftJobService(
        UUID cpuId,
        ICraftPlanner planner,
        Supplier<ICraftExecutor> executorFactory,
        Supplier<List<CraftAmount>> availableSupplier) {

        this.cpuId = cpuId;
        this.planner = planner;
        this.executorFactory = executorFactory;
        this.availableSupplier = availableSupplier;
    }

    public AutocraftJobService(
        ICraftPlanner planner,
        Supplier<ICraftExecutor> executorFactory,
        Supplier<List<CraftAmount>> availableSupplier) {

        this(UUID.randomUUID(), planner, executorFactory, availableSupplier);
    }

    public UUID cpuId() {
        return cpuId;
    }

    public boolean isBusy() {
        return runningJobId != null || !queued.isEmpty();
    }

    public Optional<RunningSnapshot> snapshotRunning() {
        if (runningJobId == null || runningExecutor == null) {
            return Optional.empty();
        }
        if (!(runningExecutor instanceof SequentialCraftExecutor sequential)) {
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
            sequential.nextStepIndex()));
    }

    public void restoreRunning(RunningSnapshot snapshot) {
        if (!snapshot.cpuId().equals(cpuId) || runningJobId != null || !queued.isEmpty()) {
            return;
        }
        var executor = executorFactory.get();
        if (!(executor instanceof SequentialCraftExecutor sequential)) {
            return;
        }
        sequential.start(snapshot.plan(), snapshot.nextStepIndex());
        jobs.put(snapshot.jobId(), new AutocraftJob(
            snapshot.jobId(),
            snapshot.targets(),
            AutocraftJob.Status.RUNNING,
            null,
            null));
        runningJobId = snapshot.jobId();
        runningExecutor = executor;
    }

    public Optional<CompoundTag> serializeRunningSnapshot(PatternNbtCodec codec) {
        return snapshotRunning().map(snapshot -> serializeSnapshot(snapshot, codec));
    }

    public void restoreRunningSnapshot(CompoundTag tag, PatternNbtCodec codec) {
        restoreRunning(deserializeSnapshot(tag, codec));
    }

    public UUID submit(List<CraftAmount> targets) {
        if (isBusy()) {
            throw new IllegalStateException("CPU_BUSY");
        }
        var id = UUID.randomUUID();
        jobs.put(id, new AutocraftJob(id, targets, AutocraftJob.Status.QUEUED, null, null));
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
            jobs.put(id, new AutocraftJob(
                id,
                current.targets(),
                AutocraftJob.Status.CANCELLED,
                current.planError(),
                null));
            return true;
        }
        if (current.status() == AutocraftJob.Status.RUNNING && id.equals(runningJobId)) {
            if (runningExecutor != null) {
                runningExecutor.cancel();
            }
            jobs.put(id, new AutocraftJob(
                id,
                current.targets(),
                AutocraftJob.Status.CANCELLED,
                current.planError(),
                null));
            runningJobId = null;
            runningExecutor = null;
            return true;
        }
        return false;
    }

    public void tick() {
        if (runningJobId == null) {
            startNextJob();
            return;
        }
        if (runningJobId == null || runningExecutor == null) {
            return;
        }

        runningExecutor.tick();
        var state = runningExecutor.state();
        if (state == ExecutionState.RUNNING || state == ExecutionState.IDLE) {
            return;
        }

        var current = jobs.get(runningJobId);
        if (current == null) {
            runningJobId = null;
            runningExecutor = null;
            return;
        }
        if (state == ExecutionState.COMPLETED) {
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                AutocraftJob.Status.DONE,
                current.planError(),
                null));
        } else if (state == ExecutionState.BLOCKED) {
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                AutocraftJob.Status.BLOCKED,
                current.planError(),
                runningExecutor.error()));
        } else {
            jobs.put(current.id(), new AutocraftJob(
                current.id(),
                current.targets(),
                AutocraftJob.Status.FAILED,
                current.planError(),
                runningExecutor.error()));
        }
        runningJobId = null;
        runningExecutor = null;
    }

    private void startNextJob() {
        var id = queued.poll();
        if (id == null) {
            return;
        }
        var current = jobs.get(id);
        if (current == null) {
            return;
        }

        var result = planner.plan(current.targets(), availableSupplier.get());
        if (!result.isSuccess()) {
            jobs.put(id, new AutocraftJob(id, current.targets(), AutocraftJob.Status.FAILED, result.error(), null));
            return;
        }

        var executor = executorFactory.get();
        executor.start(result.plan());
        jobs.put(id, new AutocraftJob(id, current.targets(), AutocraftJob.Status.RUNNING, null, null));

        runningJobId = id;
        runningExecutor = executor;
    }

    public record RunningSnapshot(
        UUID cpuId,
        UUID jobId,
        List<CraftAmount> targets,
        CraftPlan plan,
        int nextStepIndex) {}

    private static CompoundTag serializeSnapshot(RunningSnapshot snapshot, PatternNbtCodec codec) {
        var tag = new CompoundTag();
        tag.putUUID("cpuId", snapshot.cpuId());
        tag.putUUID("jobId", snapshot.jobId());
        tag.put("targets", serializeAmounts(snapshot.targets()));
        tag.put("plan", serializePlan(snapshot.plan(), codec));
        tag.putInt("nextStepIndex", snapshot.nextStepIndex());
        return tag;
    }

    private static RunningSnapshot deserializeSnapshot(CompoundTag tag, PatternNbtCodec codec) {
        return new RunningSnapshot(
            tag.getUUID("cpuId"),
            tag.getUUID("jobId"),
            deserializeAmounts(tag.getList("targets", TAG_COMPOUND)),
            deserializePlan(tag.getCompound("plan"), codec),
            tag.getInt("nextStepIndex"));
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
        var key = type == CraftKey.Type.FLUID ?
            CraftKey.fluid(tag.getString("id"), tag.getString("nbt")) :
            CraftKey.item(tag.getString("id"), tag.getString("nbt"));
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
                stepTag.getLong("runs")));
        }
        return new CraftPlan(out);
    }
}
