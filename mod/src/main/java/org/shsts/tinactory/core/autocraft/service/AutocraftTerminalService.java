package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
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

    public long requestablesRevision() {
        return patternRepository.revision();
    }

    public List<CpuStatusEntry> listCpuStatuses() {
        return cpuRuntime.listVisibleCpus().stream()
            .map(this::toCpuStatus)
            .toList();
    }

    public void cancelCpu(UUID cpuId) {
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return;
        }
        var job = service.get().getJob();
        if (job.isEmpty()) {
            return;
        }
        var status = job.get().state();
        if (status != JobState.RUNNING && status != JobState.BLOCKED) {
            return;
        }
        service.get().cancel();
    }

    public AutocraftPreview preview(IStackKey target, long quantity) {
        if (quantity <= 0L) {
            clearPreview();
            return previewResult;
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var result = planner.plan(targets);
        if (result.plan() == null) {
            previewTargets = null;
            previewResult = AutocraftPreview.failure(result.error(), result.summary());
            return previewResult;
        }
        previewTargets = targets;
        previewResult = AutocraftPreview.success(result.plan(), result.summary());
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
        service.get().submitPrepared(targets, plan);
        return AutocraftExecuteResult.success();
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

    private CpuStatusEntry toCpuStatus(UUID cpuId) {
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return CpuStatusEntry.offline(cpuId);
        }

        var current = service.get().getJob();
        if (current.isEmpty()) {
            return CpuStatusEntry.idle(cpuId);
        }
        var job = current.get();
        return new CpuStatusEntry(
            cpuId,
            job.state(),
            job.targets(),
            job.completedSteps(),
            job.totalSteps(),
            job.error());
    }
}
