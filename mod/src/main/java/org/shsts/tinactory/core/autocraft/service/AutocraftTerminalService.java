package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalService {
    private final ICraftPlanner planner;
    private final Supplier<List<CraftPattern>> patternSupplier;
    private final Supplier<List<UUID>> visibleCpuSupplier;
    private final Supplier<List<UUID>> availableCpuSupplier;
    private final Supplier<List<CraftAmount>> availableSupplier;
    @Nullable
    private AutocraftPreview preview;
    private final Function<UUID, AutocraftJobService> jobServiceResolver;

    public AutocraftTerminalService(
        ICraftPlanner planner,
        Supplier<List<CraftPattern>> patternSupplier,
        Supplier<List<UUID>> visibleCpuSupplier,
        Supplier<List<UUID>> availableCpuSupplier,
        Supplier<List<CraftAmount>> availableSupplier) {

        this(planner, patternSupplier, visibleCpuSupplier, availableCpuSupplier, availableSupplier,
            $ -> null);
    }

    public AutocraftTerminalService(
        ICraftPlanner planner,
        Supplier<List<CraftPattern>> patternSupplier,
        Supplier<List<UUID>> visibleCpuSupplier,
        Supplier<List<UUID>> availableCpuSupplier,
        Supplier<List<CraftAmount>> availableSupplier,
        Function<UUID, AutocraftJobService> jobServiceResolver) {

        this.planner = planner;
        this.patternSupplier = patternSupplier;
        this.visibleCpuSupplier = visibleCpuSupplier;
        this.availableCpuSupplier = availableCpuSupplier;
        this.availableSupplier = availableSupplier;
        this.jobServiceResolver = jobServiceResolver;
    }

    public List<CraftKey> listRequestables() {
        var dedup = patternSupplier.get().stream()
            .flatMap(pattern -> pattern.outputs().stream())
            .map(CraftAmount::key)
            .collect(Collectors.toSet());
        return dedup.stream()
            .sorted(Comparator.comparing(CraftKey::type)
                .thenComparing(CraftKey::id)
                .thenComparing(CraftKey::nbt))
            .toList();
    }

    public List<UUID> listAvailableCpus() {
        return List.copyOf(availableCpuSupplier.get());
    }

    public List<CpuStatusEntry> listCpuStatuses() {
        var available = availableCpuSupplier.get();
        return visibleCpuSupplier.get().stream()
            .map(cpuId -> toCpuStatus(cpuId, available))
            .toList();
    }

    public boolean cancelCpu(UUID cpuId) {
        var service = jobServiceResolver.apply(cpuId);
        if (service == null) {
            return false;
        }
        var running = service.listJobs().stream()
            .filter(job -> job.status() == AutocraftJob.Status.RUNNING || job.status() == AutocraftJob.Status.BLOCKED)
            .findFirst();
        return running.isPresent() && service.cancel(running.get().id());
    }

    public AutocraftPreviewResult preview(CraftKey target, long quantity) {
        if (quantity <= 0L) {
            return AutocraftPreviewResult.failure(AutocraftPreviewResult.Code.INVALID_REQUEST);
        }
        var targets = List.of(new CraftAmount(target, quantity));
        var result = planner.plan(targets, availableSupplier.get());
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
        var service = jobServiceResolver.apply(cpuId);
        if (service == null) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.CPU_OFFLINE);
        }
        if (service.isBusy()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteResult.Code.CPU_BUSY);
        }
        var preview1 = preview;
        preview = null;
        return AutocraftExecuteResult.success(
            service.submitPrepared(preview1.targets(), preview1.planSnapshot()));
    }

    public void cancelPreview() {
        preview = null;
    }

    public Optional<AutocraftPreview> preview() {
        return Optional.ofNullable(preview);
    }

    private CpuStatusEntry toCpuStatus(UUID cpuId, List<UUID> available) {
        var service = jobServiceResolver.apply(cpuId);
        if (service == null) {
            return new CpuStatusEntry(cpuId, false, "Offline", "N/A", "CPU service unavailable", false);
        }

        var current = currentJob(service);
        var target = current.map(this::formatTargetSummary).orElse("Idle");
        var step = current.map(job -> formatCurrentStep(service, job)).orElse("Idle");
        var blocked = current.map(AutocraftTerminalService::formatBlockedReason).orElse("");
        var cancellable = current
            .map(job -> job.status() == AutocraftJob.Status.RUNNING || job.status() == AutocraftJob.Status.BLOCKED)
            .orElse(false);
        return new CpuStatusEntry(cpuId, available.contains(cpuId), target, step, blocked, cancellable);
    }

    private static Optional<AutocraftJob> currentJob(AutocraftJobService service) {
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
        return first.amount() + "x " + first.key().id();
    }

    private static String formatCurrentStep(AutocraftJobService service, AutocraftJob job) {
        var details = job.executionDetails();
        if (details == null) {
            return job.status() == AutocraftJob.Status.QUEUED ? "Queued" : "N/A";
        }
        var snapshot = service.snapshotRunning();
        if (snapshot.isPresent()) {
            var next = details.nextStepIndex() + 1;
            return next + "/" + snapshot.get().plan().steps().size();
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
