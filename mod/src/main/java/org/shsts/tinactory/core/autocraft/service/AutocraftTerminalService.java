package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.api.PlanningState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalService {
    private final ICraftPlanner planner;
    private final IPatternRepository patternRepository;
    private final ICpuRuntime cpuRuntime;
    private AutocraftPreviewResult previewResult = AutocraftPreviewResult.empty();

    public AutocraftTerminalService(
        ICraftPlanner planner,
        IPatternRepository patternRepository,
        ICpuRuntime cpuRuntime) {

        this.planner = planner;
        this.patternRepository = patternRepository;
        this.cpuRuntime = cpuRuntime;
    }

    public List<IIngredientKey> listRequestables() {
        return patternRepository.listRequestables();
    }

    public List<UUID> listAvailableCpus() {
        return List.copyOf(cpuRuntime.listAvailableCpus());
    }

    public List<CpuStatusEntry> listCpuStatuses() {
        var available = cpuRuntime.listAvailableCpus();
        return cpuRuntime.listVisibleCpus().stream()
            .map(cpuId -> toCpuStatus(cpuId, available))
            .toList();
    }

    public boolean cancelCpu(UUID cpuId) {
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return false;
        }
        var job = service.get().getJob();
        if (job.isEmpty()) {
            return false;
        }
        var status = job.get().execution().state();
        if (status != JobState.RUNNING && status != JobState.BLOCKED) {
            return false;
        }
        return service.get().cancel(job.get().jobId());
    }

    public AutocraftPreviewResult preview(IIngredientKey target, long quantity) {
        if (quantity <= 0L) {
            previewResult = AutocraftPreviewResult.empty();
            return previewResult;
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var snapshot = planner.plan(targets);
        if (snapshot.state() != PlanningState.COMPLETED || snapshot.plan() == null) {
            previewResult = AutocraftPreviewResult.failure(snapshot.error());
            return previewResult;
        }
        previewResult = AutocraftPreviewResult.success(new AutocraftPreview(targets, snapshot.plan()));
        return previewResult;
    }

    public AutocraftExecuteResult execute(UUID cpuId) {
        var preview = previewResult.preview();
        if (preview == null) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.PLAN_NOT_FOUND);
        }
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.CPU_OFFLINE);
        }
        if (service.get().isBusy()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.CPU_BUSY);
        }
        previewResult = AutocraftPreviewResult.empty();
        return AutocraftExecuteResult.success(service.get().submitPrepared(preview.targets(), preview.planSnapshot()));
    }

    public void cancelPreview() {
        previewResult = AutocraftPreviewResult.empty();
    }

    public Optional<AutocraftPreview> preview() {
        return Optional.ofNullable(previewResult.preview());
    }

    public AutocraftPreviewResult previewResult() {
        return previewResult;
    }

    private CpuStatusEntry toCpuStatus(UUID cpuId, List<UUID> available) {
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return CpuStatusEntry.idle(cpuId, false);
        }

        var current = service.get().getJob();
        if (current.isEmpty()) {
            return CpuStatusEntry.idle(cpuId, available.contains(cpuId));
        }
        var execution = current.get().execution();
        var state = execution.state();
        return new CpuStatusEntry(
            cpuId,
            available.contains(cpuId),
            current.get().targets(),
            state,
            state == JobState.IDLE ? null : execution.phase(),
            execution.nextStepIndex(),
            execution.plan().steps().size(),
            execution.error(),
            state == JobState.RUNNING || state == JobState.BLOCKED);
    }

    public record CpuStatusEntry(
        UUID cpuId,
        boolean available,
        List<CraftAmount> targets,
        JobState state,
        @Nullable ExecutionPhase phase,
        int nextStepIndex,
        int stepCount,
        ExecutionError error,
        boolean cancellable) {

        public CpuStatusEntry {
            targets = List.copyOf(targets);
        }

        public static CpuStatusEntry idle(UUID cpuId, boolean available) {
            return new CpuStatusEntry(
                cpuId,
                available,
                List.of(),
                JobState.IDLE,
                null,
                0,
                0,
                ExecutionError.NONE,
                false);
        }
    }
}
