package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
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
    private final long bytesPerStep;
    private final long bytesPerItem;
    private final long bytesPerItemType;
    private final long bytesPerFluid;
    private final long bytesPerFluidType;
    private AutocraftPreview previewResult = AutocraftPreview.empty();
    @Nullable
    private List<CraftAmount> previewTargets;

    public AutocraftTerminalService(
        ICraftPlanner planner,
        IPatternRepository patternRepository,
        ICpuRuntime cpuRuntime,
        long bytesPerStep,
        long bytesPerItem,
        long bytesPerItemType,
        long bytesPerFluid,
        long bytesPerFluidType) {

        this.planner = planner;
        this.patternRepository = patternRepository;
        this.cpuRuntime = cpuRuntime;
        this.bytesPerStep = Math.max(0L, bytesPerStep);
        this.bytesPerItem = Math.max(0L, bytesPerItem);
        this.bytesPerItemType = Math.max(0L, bytesPerItemType);
        this.bytesPerFluid = Math.max(0L, bytesPerFluid);
        this.bytesPerFluidType = Math.max(0L, bytesPerFluidType);
    }

    public AutocraftTerminalService(
        ICraftPlanner planner,
        IPatternRepository patternRepository,
        ICpuRuntime cpuRuntime) {

        this(planner, patternRepository, cpuRuntime, 0L, 0L, 0L, 0L, 0L);
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
        previewResult = AutocraftPreview.success(
            result.plan(),
            result.summary(),
            calculateMemoryUsage(result.plan(), result.summary()));
        return previewResult;
    }

    public boolean execute(UUID cpuId) {
        if (!previewResult.isSuccess() || previewTargets == null || previewResult.planSnapshot() == null) {
            return false;
        }
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return false;
        }
        if (service.get().isBusy()) {
            return false;
        }
        if (previewResult.memoryUsage() > service.get().memoryLimit()) {
            return false;
        }
        var targets = previewTargets;
        var plan = previewResult.planSnapshot();
        var memoryUsage = previewResult.memoryUsage();
        clearPreview();
        service.get().submitPrepared(targets, plan, memoryUsage);
        return true;
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

    private long calculateMemoryUsage(CraftPlan planSnapshot, PlanSummary summary) {
        var ret = bytesPerStep * planSnapshot.steps().size();
        for (var entry : summary.entries().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var amount = value.consumedFromInventory() + value.craftedAmount();
            if (key.type() == PortType.ITEM) {
                ret += bytesPerItemType + bytesPerItem * amount;
            } else if (key.type() == PortType.FLUID) {
                ret += bytesPerFluidType + bytesPerFluid * amount;
            }
        }
        return ret;
    }
}
