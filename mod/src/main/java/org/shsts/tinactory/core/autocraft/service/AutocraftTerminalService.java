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
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalService {
    private final ICraftPlanner planner;
    private final IPatternRepository patternRepository;
    private final ICpuRuntime cpuRuntime;
    private Optional<PlanResult> previewResult = Optional.empty();
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

    public PlanResult preview(IStackKey target, long quantity) {
        if (quantity <= 0L) {
            clearPreview();
            return PlanResult.failed(PlanError.missingPattern(target), PlanSummary.empty());
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var result = planner.plan(targets);
        previewResult = Optional.of(result);
        if (result.plan() == null) {
            previewTargets = null;
            return result;
        }
        previewTargets = targets;
        return result;
    }

    public boolean execute(UUID cpuId) {
        if (previewResult.isEmpty() || previewTargets == null || previewResult.get().plan() == null) {
            return false;
        }
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return false;
        }
        if (service.get().isBusy()) {
            return false;
        }
        var plan = previewResult.get().plan();
        if (plan.memoryUsage() > service.get().memoryLimit()) {
            return false;
        }
        var targets = previewTargets;
        service.get().submitPrepared(targets, plan);
        clearPreview();
        return true;
    }

    public Optional<PlanResult> preview() {
        return previewResult.filter(result -> result.plan() != null);
    }

    public Optional<PlanResult> previewResult() {
        return previewResult;
    }

    private void clearPreview() {
        previewResult = Optional.empty();
        previewTargets = null;
    }

    private CpuStatusEntry toCpuStatus(UUID cpuId) {
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return CpuStatusEntry.offline(cpuId);
        }

        var current = service.get().getJob();
        if (current.isEmpty()) {
            return CpuStatusEntry.idle(cpuId, service.get().memoryLimit());
        }
        var job = current.get();
        return new CpuStatusEntry(
            cpuId,
            job.state(),
            job.targets(),
            job.completedSteps(),
            job.totalSteps(),
            job.error(),
            service.get().memoryLimit(),
            job.memoryUsage());
    }

}
