package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void serviceShouldRemainRunningWhenExecutorBlockedRetriably() {
        var planner = new TestPlanner(PlanResult.success(new CraftPlan(List.of(step()))));
        var executor = new TestExecutor(ExecutionState.BLOCKED, ExecutionState.BLOCKED, ExecutionState.COMPLETED);
        executor.blockedReason = ExecutionError.Code.FLUSH_BACKPRESSURE;
        var service = new AutocraftJobService(planner, () -> executor, List::of);

        var id = service.submit(List.of(new CraftAmount(CraftKey.item("x:y", ""), 1)));
        service.tick();
        service.tick();
        assertEquals(AutocraftJob.Status.BLOCKED, service.job(id).status());
        service.tick();
        service.tick();
        assertEquals(AutocraftJob.Status.DONE, service.job(id).status());
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
        assertThrows(IllegalStateException.class, () ->
            service.submit(List.of(new CraftAmount(CraftKey.item("x:y2", ""), 1))));

        assertEquals(
            List.of(first),
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
        var executor = new TestExecutor(ExecutionState.RUNNING, ExecutionState.CANCELLED);
        var service = new AutocraftJobService(planner, () -> executor, List::of);
        var id = service.submit(List.of(new CraftAmount(CraftKey.item("x:y", ""), 1)));
        service.tick();

        assertTrue(service.cancel(id));
        service.tick();
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
        private int index;
        private boolean cancelled;
        private ExecutionError.Code blockedReason;

        private TestExecutor(ExecutionState... states) {
            this.states = List.of(states);
        }

        @Override
        public void start(CraftPlan plan) {}

        @Override
        public void runCycle(long transmissionBandwidth) {
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
            if (state() != ExecutionState.BLOCKED && state() != ExecutionState.CANCELLED) {
                return null;
            }
            var code = state() == ExecutionState.CANCELLED ? ExecutionError.Code.CANCELLED : blockedReason;
            return new ExecutionError(code == null ? ExecutionError.Code.MACHINE_UNAVAILABLE : code, "s1", "blocked");
        }

        @Override
        public ExecutionDetails details() {
            return new ExecutionDetails(
                state() == ExecutionState.CANCELLED ? ExecutionDetails.Phase.TERMINAL : ExecutionDetails.Phase.RUN_STEP,
                state() == ExecutionState.BLOCKED ? blockedReason : null,
                state() == ExecutionState.CANCELLED ? ExecutionState.CANCELLED : null,
                0,
                Map.of(),
                Map.of(),
                Map.of(),
                null);
        }
    }
}
