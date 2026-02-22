package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftJobService {
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
        ICraftPlanner planner,
        Supplier<ICraftExecutor> executorFactory,
        Supplier<List<CraftAmount>> availableSupplier) {

        this.planner = planner;
        this.executorFactory = executorFactory;
        this.availableSupplier = availableSupplier;
    }

    public UUID submit(List<CraftAmount> targets) {
        var id = UUID.randomUUID();
        jobs.put(id, new AutocraftJob(id, targets, AutocraftJob.Status.QUEUED, null, null));
        queued.add(id);
        return id;
    }

    public AutocraftJob job(UUID id) {
        return jobs.get(id);
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
}
