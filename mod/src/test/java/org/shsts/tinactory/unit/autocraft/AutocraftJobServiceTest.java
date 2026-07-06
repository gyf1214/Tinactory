package org.shsts.tinactory.unit.autocraft;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestAutocraftHelper.PATTERN_CODECS;

class AutocraftJobServiceTest {
    @Test
    void serviceShouldClearJobWhenRunningDone() {
        var executor = new TestExecutor(JobState.RUNNING, JobState.IDLE);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);

        service.submitPrepared(List.of(target), testPlan());

        assertEquals(JobState.RUNNING, service.getJob().orElseThrow().state());
        service.tick();
        assertTrue(service.getJob().isEmpty());
    }

    @Test
    void serviceShouldSubmitPreparedPlanWithoutPlannerDependency() {
        var service = new AutocraftJobService(new TestExecutor(JobState.IDLE));

        service.submitPrepared(
            List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)),
            testPlan());

        assertTrue(service.getJob().isEmpty());
    }

    @Test
    void serviceShouldRemainBusyWhenExecutorBlockedRetriably() {
        var executor = new TestExecutor(JobState.BLOCKED, JobState.BLOCKED, JobState.IDLE);
        executor.blockedReason = ExecutionError.FLUSH_BLOCKED;
        var service = new AutocraftJobService(executor);

        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());
        service.tick();
        assertEquals(JobState.BLOCKED, service.getJob().orElseThrow().state());
        assertTrue(service.isBusy());
        service.tick();
        assertTrue(service.getJob().isEmpty());
    }

    @Test
    void serviceShouldForwardItemAndFluidBandwidths() {
        var executor = new TestExecutor(JobState.RUNNING);
        var service = new AutocraftJobService(executor, 16, 4000, 1);

        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());
        service.tick();

        assertEquals(16L, executor.lastItemBandwidth);
        assertEquals(4000L, executor.lastFluidBandwidth);
    }

    @Test
    void serviceShouldExposeEmptyWhenNoCurrentJob() {
        var service = new AutocraftJobService(new TestExecutor(JobState.IDLE));

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
        var service = new AutocraftJobService(new TestExecutor(JobState.RUNNING, JobState.IDLE));
        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());

        assertTrue(service.cancel());
        service.tick();
        assertTrue(service.getJob().isEmpty());
    }

    @Test
    void serviceShouldCancelRunningJob() {
        var executor = new TestExecutor(JobState.RUNNING, JobState.IDLE);
        var service = new AutocraftJobService(executor);
        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("x:y", ""), 1)), testPlan());

        assertTrue(service.cancel());
        service.tick();
        assertTrue(service.getJob().isEmpty());
        assertTrue(executor.cancelled);
        assertFalse(service.cancel());
    }

    @Test
    void serviceShouldExposePublicJobSnapshotFromCheapExecutorFields() {
        var executor = new TestExecutor(JobState.RUNNING);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);

        service.submitPrepared(List.of(target), testPlan());
        var snapshot = service.getJob().orElseThrow();

        assertEquals(List.of(target), snapshot.targets());
        assertEquals(JobState.RUNNING, snapshot.state());
        assertEquals(0, snapshot.completedSteps());
        assertEquals(1, snapshot.totalSteps());
        assertEquals(ExecutionError.NONE, snapshot.error());
    }

    @Test
    void serviceShouldReadMemoryUsageFromPreparedPlan() {
        var executor = new TestExecutor(JobState.RUNNING);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);

        service.submitPrepared(List.of(target), testPlan(256L));

        assertEquals(256L, service.getJob().orElseThrow().memoryUsage());
    }

    @Test
    void serviceShouldRestoreRunningSnapshotThroughExecutorPersistence() {
        var executor = new TestExecutor(JobState.RUNNING);
        var service = new AutocraftJobService(executor);
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);

        service.submitPrepared(List.of(target), testPlan());
        var persisted = service.serializeRunningSnapshot(PATTERN_CODECS).orElseThrow();
        var restoredExecutor = new TestExecutor(JobState.IDLE);
        service = new AutocraftJobService(restoredExecutor);
        service.restoreRunningSnapshot(persisted, PATTERN_CODECS);

        assertTrue(restoredExecutor.restoreCalled);
        assertTrue(service.isBusy());
        assertEquals(JobState.RUNNING, service.getJob().orElseThrow().state());
        assertEquals(List.of(target), service.getJob().orElseThrow().targets());
    }

    private static CraftStep step() {
        return new CraftStep("s1", new CraftPattern(
            TestAutocraftHelper.uuid("tinactory:test"),
            List.of(new CraftAmount(TestStackKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            TestAutocraftHelper.constraints("tinactory:mixer", 0)), 1);
    }

    private static CraftPlan testPlan() {
        return testPlan(0L);
    }

    private static CraftPlan testPlan(long memoryUsage) {
        return new CraftPlan(List.of(step()), PlanSummary.empty(), memoryUsage);
    }

    private static final class TestExecutor implements ICraftExecutor {
        private final List<JobState> states;
        private int index;
        private boolean cancelled;
        private boolean restoreCalled;
        private ExecutionError blockedReason;
        private CraftPlan currentPlan = new CraftPlan(List.of());
        private long lastItemBandwidth;
        private long lastFluidBandwidth;

        private TestExecutor(JobState... states) {
            this.states = List.of(states);
        }

        @Override
        public void start(CraftPlan plan) {
            currentPlan = plan;
        }

        @Override
        public void restore(CompoundTag tag, PatternCodec codec) {
            restoreCalled = true;
        }

        @Override
        public void runCycle(long itemBandwidth, long fluidBandwidth) {
            lastItemBandwidth = itemBandwidth;
            lastFluidBandwidth = fluidBandwidth;
            if (index + 1 < states.size()) {
                index++;
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isBusy() {
            return state().busy();
        }

        @Override
        public JobState state() {
            return restoreCalled ? JobState.RUNNING : states.get(index);
        }

        @Override
        public ExecutionError error() {
            return errorFor(state());
        }

        @Override
        public int completedSteps() {
            return 0;
        }

        @Override
        public int totalSteps() {
            return currentPlan.steps().size();
        }

        @Override
        public CompoundTag serialize(PatternCodec codec) {
            return new CompoundTag();
        }

        private ExecutionError errorFor(JobState jobState) {
            if (jobState == JobState.BLOCKED) {
                return blockedReason == null ? ExecutionError.MACHINE_UNAVAILABLE : blockedReason;
            }
            return ExecutionError.NONE;
        }
    }
}
