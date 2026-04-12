package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestStackKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutorSnapshot;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
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
        var executor = new TestExecutor(JobState.RUNNING, JobState.COMPLETED);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);

        var id = service.submitPrepared(List.of(target), testPlan());

        assertEquals(JobState.RUNNING, service.getJob().orElseThrow().execution().state());
        service.tick();
        assertEquals(JobState.COMPLETED, service.getJob().orElseThrow().execution().state());
        assertEquals(id, service.getJob().orElseThrow().jobId());
    }

    @Test
    void serviceShouldSubmitPreparedPlanWithoutPlannerDependency() {
        var service = new AutocraftJobService(new TestExecutor(JobState.COMPLETED));

        var id = service.submitPrepared(
            List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)),
            testPlan());

        assertEquals(id, service.getJob().orElseThrow().jobId());
        assertEquals(JobState.COMPLETED, service.getJob().orElseThrow().execution().state());
    }

    @Test
    void serviceShouldRemainBusyWhenExecutorBlockedRetriably() {
        var executor = new TestExecutor(JobState.BLOCKED, JobState.BLOCKED, JobState.COMPLETED);
        executor.blockedReason = ExecutionError.FLUSH_BACKPRESSURE;
        var service = new AutocraftJobService(executor);

        var id = service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());
        service.tick();
        assertEquals(JobState.BLOCKED, service.getJob().orElseThrow().execution().state());
        assertTrue(service.isBusy());
        service.tick();
        assertEquals(JobState.COMPLETED, service.getJob().orElseThrow().execution().state());
        assertEquals(id, service.getJob().orElseThrow().jobId());
    }

    @Test
    void serviceShouldExposeEmptyWhenNoCurrentJob() {
        var service = new AutocraftJobService(new TestExecutor(JobState.COMPLETED));

        assertTrue(service.getJob().isEmpty());
    }

    @Test
    void serviceShouldRejectSubmitWhenCurrentJobExists() {
        var service = new AutocraftJobService(new TestExecutor(JobState.RUNNING));

        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y1", ""), 1)), testPlan());
        assertThrows(IllegalStateException.class, () ->
            service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y2", ""), 1)), testPlan()));
    }

    @Test
    void serviceShouldCancelRunningJobBeforeFirstTick() {
        var service = new AutocraftJobService(new TestExecutor(JobState.RUNNING, JobState.CANCELLED));
        var id = service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());

        assertFalse(service.cancel(UUIDs.other()));
        assertTrue(service.cancel(id));
        service.tick();
        assertEquals(JobState.CANCELLED, service.getJob().orElseThrow().execution().state());
    }

    @Test
    void serviceShouldCancelRunningJob() {
        var executor = new TestExecutor(JobState.RUNNING, JobState.CANCELLED);
        var service = new AutocraftJobService(executor);
        var id = service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());

        assertTrue(service.cancel(id));
        service.tick();
        assertEquals(JobState.CANCELLED, service.getJob().orElseThrow().execution().state());
        assertTrue(executor.cancelled);
        assertFalse(service.cancel(id));
    }

    @Test
    void serviceShouldExposeRunningSnapshotFromExecutorInterface() {
        var executor = new TestExecutor(JobState.RUNNING);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);

        var jobId = service.submitPrepared(List.of(target), testPlan());
        var snapshot = service.snapshotRunning().orElseThrow();

        assertEquals(jobId, snapshot.jobId());
        assertEquals(executor.snapshot(), snapshot.execution());
        assertEquals(testPlan(), snapshot.execution().plan());
    }

    @Test
    void serviceShouldRestoreRunningSnapshotThroughExecutorInterface() {
        var executor = new TestExecutor(JobState.RUNNING);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);
        var snapshot = new ExecutorSnapshot(
            JobState.RUNNING,
            ExecutionPhase.RUN_STEP,
            ExecutionError.NONE,
            null,
            testPlan(),
            0,
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            null);

        service.restoreRunning(new AutocraftJobService.RunningSnapshot(
            UUIDs.fixed(),
            List.of(target),
            snapshot));

        assertTrue(executor.restoreCalled);
        assertEquals(testPlan(), executor.snapshot().plan());
        assertTrue(service.isBusy());
        assertEquals(JobState.RUNNING, service.getJob().orElseThrow().execution().state());
    }

    private static CraftStep step() {
        return new CraftStep("s1", new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(TestStackKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())), 1);
    }

    private static CraftPlan testPlan() {
        return new CraftPlan(List.of(step()));
    }

    private static final class TestExecutor implements ICraftExecutor {
        private final List<JobState> states;
        private int index;
        private boolean cancelled;
        private boolean restoreCalled;
        private ExecutionError blockedReason;
        private CraftPlan currentPlan = new CraftPlan(List.of());
        private ExecutorSnapshot snapshot = snapshotFor(JobState.IDLE, currentPlan, null);

        private TestExecutor(JobState... states) {
            this.states = List.of(states);
        }

        @Override
        public void start(CraftPlan plan) {
            currentPlan = plan;
            snapshot = snapshotFor(state(), currentPlan, null);
        }

        @Override
        public void restore(ExecutorSnapshot snapshot) {
            restoreCalled = true;
            currentPlan = snapshot.plan();
            this.snapshot = snapshot;
        }

        @Override
        public void runCycle(long transmissionBandwidth) {
            if (index + 1 < states.size()) {
                index++;
            }
            snapshot = snapshotFor(state(), currentPlan, snapshot.leasedMachineId());
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public ExecutorSnapshot snapshot() {
            return snapshot;
        }

        private JobState state() {
            return states.get(index);
        }

        private ExecutorSnapshot snapshotFor(JobState state, CraftPlan plan, java.util.UUID leasedMachineId) {
            var error = errorFor(state);
            return new ExecutorSnapshot(
                state,
                state == JobState.CANCELLED ? ExecutionPhase.TERMINAL : ExecutionPhase.RUN_STEP,
                error,
                null,
                plan,
                0,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                leasedMachineId);
        }

        private ExecutionError errorFor(JobState state) {
            if (state == JobState.BLOCKED) {
                return blockedReason == null ? ExecutionError.MACHINE_UNAVAILABLE : blockedReason;
            }
            return ExecutionError.NONE;
        }
    }

    private static final class UUIDs {
        private static java.util.UUID fixed() {
            return java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
        }

        private static java.util.UUID other() {
            return java.util.UUID.fromString("22222222-2222-2222-2222-222222222222");
        }
    }
}
