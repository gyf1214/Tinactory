package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJob;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftJobServiceTest {
    @Test
    void serviceShouldTransitionQueuedRunningDone() {
        var planner = new TestPlanner(PlanResult.success(new CraftPlan(List.of(step()))));
        var executor = new TestExecutor(ExecutionState.RUNNING, ExecutionState.COMPLETED);
        var service = new AutocraftJobService(planner, () -> executor, List::of);
        var target = new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1);

        var id = service.submit(List.of(target));

        assertEquals(AutocraftJob.Status.QUEUED, service.job(id).status());
        service.tick();
        assertEquals(AutocraftJob.Status.RUNNING, service.job(id).status());
        service.tick();
        assertEquals(AutocraftJob.Status.DONE, service.job(id).status());
    }

    @Test
    void serviceShouldTransitionToFailedOnPlanError() {
        var planner = new TestPlanner(PlanResult.failure(PlanError.missingPattern(CraftKey.item("x:y", ""))));
        var service = new AutocraftJobService(planner, () -> new TestExecutor(ExecutionState.COMPLETED), List::of);

        var id = service.submit(List.of(new CraftAmount(CraftKey.item("x:y", ""), 1)));
        service.tick();

        assertEquals(AutocraftJob.Status.FAILED, service.job(id).status());
        assertEquals(PlanError.Code.MISSING_PATTERN, service.job(id).planError().code());
    }

    @Test
    void serviceShouldTransitionToBlockedOnExecutorBlocked() {
        var planner = new TestPlanner(PlanResult.success(new CraftPlan(List.of(step()))));
        var executor = new TestExecutor(ExecutionState.BLOCKED);
        var service = new AutocraftJobService(planner, () -> executor, List::of);

        var id = service.submit(List.of(new CraftAmount(CraftKey.item("x:y", ""), 1)));
        service.tick();
        service.tick();

        assertEquals(AutocraftJob.Status.BLOCKED, service.job(id).status());
    }

    @Test
    void serviceShouldExposeSafeLookupForMissingJob() {
        var service = new AutocraftJobService(
            new TestPlanner(PlanResult.success(new CraftPlan(List.of(step())))),
            () -> new TestExecutor(ExecutionState.COMPLETED),
            List::of);

        assertTrue(service.findJob(UUID.fromString("99999999-9999-9999-9999-999999999999")).isEmpty());
    }

    @Test
    void serviceShouldListJobsInSubmitOrder() {
        var planner = new TestPlanner(PlanResult.success(new CraftPlan(List.of(step()))));
        var service = new AutocraftJobService(planner, () -> new TestExecutor(ExecutionState.COMPLETED), List::of);

        var first = service.submit(List.of(new CraftAmount(CraftKey.item("x:y1", ""), 1)));
        var second = service.submit(List.of(new CraftAmount(CraftKey.item("x:y2", ""), 1)));

        assertEquals(
            List.of(first, second),
            service.listJobs().stream().map(AutocraftJob::id).collect(Collectors.toList()));
    }

    @Test
    void serviceShouldCancelQueuedJob() {
        var planner = new TestPlanner(PlanResult.success(new CraftPlan(List.of(step()))));
        var service = new AutocraftJobService(planner, () -> new TestExecutor(ExecutionState.COMPLETED), List::of);
        var id = service.submit(List.of(new CraftAmount(CraftKey.item("x:y", ""), 1)));

        assertTrue(service.cancel(id));
        assertEquals(AutocraftJob.Status.CANCELLED, service.job(id).status());
    }

    @Test
    void serviceShouldCancelRunningJob() {
        var planner = new TestPlanner(PlanResult.success(new CraftPlan(List.of(step()))));
        var executor = new TestExecutor(ExecutionState.RUNNING);
        var service = new AutocraftJobService(planner, () -> executor, List::of);
        var id = service.submit(List.of(new CraftAmount(CraftKey.item("x:y", ""), 1)));
        service.tick();

        assertTrue(service.cancel(id));
        assertEquals(AutocraftJob.Status.CANCELLED, service.job(id).status());
        assertTrue(executor.cancelled);
        assertFalse(service.cancel(id));
    }

    private static CraftStep step() {
        return new CraftStep("s1", new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())), 1);
    }

    private record TestPlanner(PlanResult result) implements ICraftPlanner {
        @Override
        public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
            return result;
        }
    }

    private static final class TestExecutor implements ICraftExecutor {
        private final List<ExecutionState> states;
        private int index = 0;
        private boolean cancelled;

        private TestExecutor(ExecutionState... states) {
            this.states = List.of(states);
        }

        @Override
        public void start(CraftPlan plan) {}

        @Override
        public void tick() {
            if (index + 1 < states.size()) {
                index++;
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public ExecutionState state() {
            return states.get(index);
        }

        @Override
        public ExecutionError error() {
            return state() == ExecutionState.BLOCKED ?
                new ExecutionError(ExecutionError.Code.MACHINE_UNAVAILABLE, "s1", "blocked") : null;
        }
    }
}
