package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalService {
    private final ICraftPlanner planner;
    private final Supplier<List<CraftPattern>> patternSupplier;
    private final Supplier<List<UUID>> visibleCpuSupplier;
    private final Supplier<List<UUID>> availableCpuSupplier;
    private final Supplier<List<CraftAmount>> availableSupplier;
    private final AutocraftPreviewSessionStore previewStore;
    private final AutocraftPlanPreflight preflight;
    private final Function<UUID, AutocraftJobService> jobServiceResolver;

    public AutocraftTerminalService(
        ICraftPlanner planner,
        Supplier<List<CraftPattern>> patternSupplier,
        Supplier<List<UUID>> visibleCpuSupplier,
        Supplier<List<UUID>> availableCpuSupplier,
        Supplier<List<CraftAmount>> availableSupplier,
        AutocraftPreviewSessionStore previewStore) {

        this(planner, patternSupplier, visibleCpuSupplier, availableCpuSupplier, availableSupplier, previewStore,
            new AutocraftPlanPreflight(), $ -> null);
    }

    public AutocraftTerminalService(
        ICraftPlanner planner,
        Supplier<List<CraftPattern>> patternSupplier,
        Supplier<List<UUID>> visibleCpuSupplier,
        Supplier<List<UUID>> availableCpuSupplier,
        Supplier<List<CraftAmount>> availableSupplier,
        AutocraftPreviewSessionStore previewStore,
        AutocraftPlanPreflight preflight,
        Function<UUID, AutocraftJobService> jobServiceResolver) {

        this.planner = planner;
        this.patternSupplier = patternSupplier;
        this.visibleCpuSupplier = visibleCpuSupplier;
        this.availableCpuSupplier = availableCpuSupplier;
        this.availableSupplier = availableSupplier;
        this.previewStore = previewStore;
        this.preflight = preflight;
        this.jobServiceResolver = jobServiceResolver;
    }

    public List<AutocraftRequestableEntry> listRequestables() {
        var dedup = new LinkedHashMap<AutocraftRequestableKey, Long>();
        for (var pattern : patternSupplier.get()) {
            for (var output : pattern.outputs()) {
                var key = AutocraftRequestableKey.fromCraftKey(output.key());
                dedup.put(key, dedup.getOrDefault(key, 0L) + 1L);
            }
        }
        return dedup.entrySet().stream()
            .map(entry -> new AutocraftRequestableEntry(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing((AutocraftRequestableEntry entry) -> entry.key().type().name())
                .thenComparing(entry -> entry.key().id())
                .thenComparing(entry -> entry.key().nbt()))
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

    public AutocraftPreviewResult preview(AutocraftPreviewRequest request) {
        if (request.quantity() <= 0L) {
            return AutocraftPreviewResult.failure(AutocraftPreviewErrorCode.INVALID_REQUEST);
        }
        if (!availableCpuSupplier.get().contains(request.cpuId())) {
            return AutocraftPreviewResult.failure(AutocraftPreviewErrorCode.CPU_BUSY);
        }
        var targets = List.of(new CraftAmount(request.target().toCraftKey(), request.quantity()));
        var result = planner.plan(targets, availableSupplier.get());
        if (!result.isSuccess() || result.plan() == null) {
            return AutocraftPreviewResult.failure(AutocraftPreviewErrorCode.PREVIEW_FAILED);
        }
        var planId = UUID.randomUUID();
        var summary = summarizeTargets(targets);
        previewStore.put(planId, request.cpuId(), targets, result.plan(), summary);
        return AutocraftPreviewResult.success(planId, result.plan(), summary);
    }

    public AutocraftExecuteResult execute(AutocraftExecuteRequest request) {
        if (request.planId() == null || request.cpuId() == null) {
            return AutocraftExecuteResult.failure(AutocraftExecuteErrorCode.INVALID_REQUEST);
        }
        var snapshot = previewStore.find(request.planId());
        if (snapshot.isEmpty()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteErrorCode.PLAN_NOT_FOUND);
        }
        if (!snapshot.get().cpuId().equals(request.cpuId()) || !availableCpuSupplier.get().contains(request.cpuId())) {
            return AutocraftExecuteResult.failure(AutocraftExecuteErrorCode.CPU_BUSY);
        }
        var service = jobServiceResolver.apply(request.cpuId());
        if (service == null || service.isBusy()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteErrorCode.CPU_BUSY);
        }
        var missingInputs = preflight.findMissingInputs(snapshot.get().planSnapshot(), availableSupplier.get());
        if (!missingInputs.isEmpty()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteErrorCode.PREFLIGHT_MISSING_INPUTS, missingInputs);
        }
        var removed = previewStore.remove(request.planId());
        if (removed.isEmpty()) {
            return AutocraftExecuteResult.failure(AutocraftExecuteErrorCode.PLAN_NOT_FOUND);
        }
        return AutocraftExecuteResult.success(
            service.submitPrepared(removed.get().targets(), removed.get().planSnapshot()));
    }

    public void cancelPreview(UUID planId) {
        previewStore.remove(planId);
    }

    public AutocraftPreviewSessionStore previewStore() {
        return previewStore;
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

    private static List<CraftAmount> summarizeTargets(List<CraftAmount> targets) {
        var merged = new LinkedHashMap<CraftKey, Long>();
        for (var target : targets) {
            merged.put(target.key(), merged.getOrDefault(target.key(), 0L) + target.amount());
        }
        return merged.entrySet().stream().map(entry -> new CraftAmount(entry.getKey(), entry.getValue())).toList();
    }
}
