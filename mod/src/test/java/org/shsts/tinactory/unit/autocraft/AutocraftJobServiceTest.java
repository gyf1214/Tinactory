package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.service.AutocraftJob;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftJobServiceTest {
    @Test
    void serviceShouldTransitionRunningDone() {
        var executor = new TestExecutor(ExecutionState.RUNNING, ExecutionState.COMPLETED);
        var service = new AutocraftJobService(() -> executor);
        var target = new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1);

        var id = service.submitPrepared(List.of(target), testPlan());

        assertEquals(AutocraftJob.Status.RUNNING, service.getJob().orElseThrow().status());
        service.tick();
        assertEquals(AutocraftJob.Status.DONE, service.getJob().orElseThrow().status());
        assertEquals(id, service.getJob().orElseThrow().id());
    }

    @Test
    void serviceShouldQueuePreparedPlanWithoutPlannerDependency() {
        var service = new AutocraftJobService(() -> new TestExecutor(ExecutionState.COMPLETED));

        var id = service.submitPrepared(
            List.of(new CraftAmount(TestIngredientKey.item("x:y", ""), 1)),
            testPlan());

        assertEquals(AutocraftJob.Status.RUNNING, service.getJob().orElseThrow().status());
        assertEquals(id, service.getJob().orElseThrow().id());
    }

    @Test
    void serviceShouldRemainRunningWhenExecutorBlockedRetriably() {
        var executor = new TestExecutor(ExecutionState.BLOCKED, ExecutionState.BLOCKED, ExecutionState.COMPLETED);
        executor.blockedReason = ExecutionError.Code.FLUSH_BACKPRESSURE;
        var service = new AutocraftJobService(() -> executor);

        var id = service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("x:y", ""), 1)), testPlan());
        service.tick();
        assertEquals(AutocraftJob.Status.BLOCKED, service.getJob().orElseThrow().status());
        service.tick();
        assertEquals(AutocraftJob.Status.DONE, service.getJob().orElseThrow().status());
        assertEquals(id, service.getJob().orElseThrow().id());
    }

    @Test
    void serviceShouldExposeEmptyWhenNoCurrentJob() {
        var service = new AutocraftJobService(() -> new TestExecutor(ExecutionState.COMPLETED));

        assertTrue(service.getJob().isEmpty());
    }

    @Test
    void serviceShouldRejectSubmitWhenCurrentJobExists() {
        var service = new AutocraftJobService(() -> new TestExecutor(ExecutionState.COMPLETED));

        service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("x:y1", ""), 1)), testPlan());
        assertThrows(IllegalStateException.class, () ->
            service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("x:y2", ""), 1)), testPlan()));
    }

    @Test
    void serviceShouldCancelRunningJobBeforeFirstTick() {
        var service = new AutocraftJobService(() -> new TestExecutor(ExecutionState.CANCELLED));
        var id = service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("x:y", ""), 1)), testPlan());

        assertTrue(service.cancel(id));
        service.tick();
        assertEquals(AutocraftJob.Status.CANCELLED, service.getJob().orElseThrow().status());
    }

    @Test
    void serviceShouldCancelRunningJob() {
        var executor = new TestExecutor(ExecutionState.RUNNING, ExecutionState.CANCELLED);
        var service = new AutocraftJobService(() -> executor);
        var id = service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("x:y", ""), 1)), testPlan());

        assertTrue(service.cancel(id));
        service.tick();
        assertEquals(AutocraftJob.Status.CANCELLED, service.getJob().orElseThrow().status());
        assertTrue(executor.cancelled);
        assertFalse(service.cancel(id));
    }

    private static CraftStep step() {
        return new CraftStep("s1", new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())), 1);
    }

    private static CraftPlan testPlan() {
        return new CraftPlan(List.of(step()));
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
