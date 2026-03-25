package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.api.PlanningState;
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
    @Nullable
    private AutocraftPreview preview;

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
            return AutocraftPreviewResult.failure(AutocraftPreviewResult.Code.INVALID_REQUEST);
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var snapshot = planner.plan(targets);
        if (snapshot.state() != PlanningState.COMPLETED || snapshot.plan() == null) {
            return AutocraftPreviewResult.failure(AutocraftPreviewResult.Code.PLAN_FAILED);
        }
        preview = new AutocraftPreview(targets, snapshot.plan());
        return AutocraftPreviewResult.success(preview);
    }

    public AutocraftExecuteResult execute(UUID cpuId) {
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
        var preview1 = preview;
        preview = null;
        return AutocraftExecuteResult.success(
            service.get().submitPrepared(preview1.targets(), preview1.planSnapshot()));
    }

    public void cancelPreview() {
        preview = null;
    }

    public Optional<AutocraftPreview> preview() {
        return Optional.ofNullable(preview);
    }

    private CpuStatusEntry toCpuStatus(UUID cpuId, List<UUID> available) {
        var service = cpuRuntime.findVisibleService(cpuId);
        if (service.isEmpty()) {
            return new CpuStatusEntry(cpuId, false, "Offline", "N/A", "CPU service unavailable", false);
        }

        var current = service.get().getJob();
        var target = current.map(this::formatTargetSummary).orElse("Idle");
        var step = current.map(AutocraftTerminalService::formatCurrentStep).orElse("Idle");
        var blocked = current.map(AutocraftTerminalService::formatBlockedReason).orElse("");
        var cancellable = current
            .map(job -> job.execution().state() == JobState.RUNNING || job.execution().state() == JobState.BLOCKED)
            .orElse(false);
        return new CpuStatusEntry(cpuId, available.contains(cpuId), target, step, blocked, cancellable);
    }

    private String formatTargetSummary(AutocraftJobSnapshot job) {
        if (job.targets().isEmpty()) {
            return job.execution().state().name();
        }
        var first = job.targets().get(0);
        return first.amount() + "x " + first.key();
    }

    private static String formatCurrentStep(AutocraftJobSnapshot job) {
        var execution = job.execution();
        var stepCount = execution.plan().steps().size();
        if (stepCount <= 0) {
            return execution.phase().name();
        }
        return execution.nextStepIndex() + 1 + "/" + stepCount;
    }

    private static String formatBlockedReason(AutocraftJobSnapshot job) {
        var error = job.execution().error();
        return error == null ? "" : error.code().name();
    }

    public record CpuStatusEntry(
        UUID cpuId,
        boolean available,
        String targetSummary,
        String currentStep,
        String blockedReason,
        boolean cancellable) {}
}
