package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.api.PlanningState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalService {
    private final ICraftPlanner planner;
    private final IPatternRepository patternRepository;
    private final ICpuRuntime cpuRuntime;
    private AutocraftPreview previewResult = AutocraftPreview.empty();
    @Nullable
    private List<CraftAmount> previewTargets;

    public AutocraftTerminalService(
        ICraftPlanner planner,
        IPatternRepository patternRepository,
        ICpuRuntime cpuRuntime) {

        this.planner = planner;
        this.patternRepository = patternRepository;
        this.cpuRuntime = cpuRuntime;
    }

    public List<IStackKey> listRequestables() {
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

    public AutocraftPreview preview(IStackKey target, long quantity) {
        if (quantity <= 0L) {
            clearPreview();
            return previewResult;
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var snapshot = planner.plan(targets);
        if (snapshot.state() != PlanningState.COMPLETED || snapshot.plan() == null) {
            previewTargets = null;
            previewResult = AutocraftPreview.failure(snapshot.error(), snapshot.summary());
            return previewResult;
        }
        previewTargets = targets;
        previewResult = AutocraftPreview.success(snapshot.plan(), snapshot.summary());
        return previewResult;
    }

    public AutocraftExecuteResult execute(UUID cpuId) {
        if (!previewResult.isSuccess() || previewTargets == null || previewResult.planSnapshot() == null) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.PLAN_NOT_FOUND);
        }
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.CPU_OFFLINE);
        }
        if (service.get().isBusy()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.CPU_BUSY);
        }
        var targets = previewTargets;
        var plan = previewResult.planSnapshot();
        clearPreview();
        return AutocraftExecuteResult.success(service.get().submitPrepared(targets, plan));
    }

    public Optional<AutocraftPreview> preview() {
        return previewResult.isSuccess() ? Optional.of(previewResult) : Optional.empty();
    }

    public AutocraftPreview previewResult() {
        return previewResult;
    }

    private void clearPreview() {
        previewResult = AutocraftPreview.empty();
        previewTargets = null;
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
}
