package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
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
        var running = service.get().listJobs().stream()
            .filter(job -> job.status() == AutocraftJob.Status.RUNNING || job.status() == AutocraftJob.Status.BLOCKED)
            .findFirst();
        return running.isPresent() && service.get().cancel(running.get().id());
    }

    public AutocraftPreviewResult preview(IIngredientKey target, long quantity) {
        if (quantity <= 0L) {
            return AutocraftPreviewResult.failure(AutocraftPreviewResult.Code.INVALID_REQUEST);
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var result = planner.plan(targets);
        if (!result.isSuccess() || result.plan() == null) {
            return AutocraftPreviewResult.failure(AutocraftPreviewResult.Code.PLAN_FAILED);
        }
        preview = new AutocraftPreview(targets, result.plan());
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

        var current = currentJob(service.get());
        var target = current.map(this::formatTargetSummary).orElse("Idle");
        var step = current.map(job -> formatCurrentStep(service.get(), job)).orElse("Idle");
        var blocked = current.map(AutocraftTerminalService::formatBlockedReason).orElse("");
        var cancellable = current
            .map(job -> job.status() == AutocraftJob.Status.RUNNING || job.status() == AutocraftJob.Status.BLOCKED)
            .orElse(false);
        return new CpuStatusEntry(cpuId, available.contains(cpuId), target, step, blocked, cancellable);
    }

    private static Optional<AutocraftJob> currentJob(IAutocraftService service) {
        return service.listJobs().stream()
            .filter(job ->
                job.status() == AutocraftJob.Status.RUNNING ||
                    job.status() == AutocraftJob.Status.BLOCKED ||
                    job.status() == AutocraftJob.Status.QUEUED)
            .findFirst();
    }

    private String formatTargetSummary(AutocraftJob job) {
        if (job.targets().isEmpty()) {
            return job.status().name();
        }
        var first = job.targets().get(0);
        return first.amount() + "x " + first.key();
    }

    private static String formatCurrentStep(IAutocraftService service, AutocraftJob job) {
        var details = job.executionDetails();
        if (details == null) {
            return job.status() == AutocraftJob.Status.QUEUED ? "Queued" : "N/A";
        }
        var stepCount = service.runningPlanStepCount();
        if (stepCount.isPresent()) {
            var next = details.nextStepIndex() + 1;
            return next + "/" + stepCount.get();
        }
        return details.phase().name();
    }

    private static String formatBlockedReason(AutocraftJob job) {
        var details = job.executionDetails();
        return details == null || details.blockedReason() == null ? "" : details.blockedReason().name();
    }

    public record CpuStatusEntry(
        UUID cpuId,
        boolean available,
        String targetSummary,
        String currentStep,
        String blockedReason,
        boolean cancellable) {}
}
