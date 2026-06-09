package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.CraftExecutor;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachineAllocator;
import org.shsts.tinactory.unit.fixture.TestMachineLease;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftExecutorTest {
    @Test
    void executorShouldRunPlanSequentiallyThroughMachineRoutes() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var gear = TestStackKey.item("tinactory:gear", "");

        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var events = new RecordingEvents();
        var allocator = new SimulatedAllocator();
        var executor = new CraftExecutor(inventory, allocator, events);
        var firstStep = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        var secondStep = new CraftStep(
            "s2",
            pattern("tinactory:gear", List.of(new CraftAmount(plate, 1)), List.of(new CraftAmount(gear, 1))),
            1);
        executor.start(new CraftPlan(List.of(firstStep, secondStep)));

        for (var i = 0; i < 12; i++) {
            executor.runCycle(64, 64);
        }

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(1L, inventory.amountOf(gear));
        assertTrue(inventory.insertCalls > 0);
        assertEquals(List.of("start:s1", "done:s1", "start:s2", "done:s2"), events.events);
    }

    @Test
    void executorShouldStartIndependentStepsUpToActiveRuntimeLimit() {
        var firstOre = TestStackKey.item("tinactory:first_ore", "");
        var secondOre = TestStackKey.item("tinactory:second_ore", "");
        var firstPlate = TestStackKey.item("tinactory:first_plate", "");
        var secondPlate = TestStackKey.item("tinactory:second_plate", "");

        var inventory = new FakeInventory(Map.of(firstOre, 1L, secondOre, 1L));
        var events = new RecordingEvents();
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), events, 2);
        var firstStep = new CraftStep(
            "s1",
            pattern(
                "tinactory:first_plate",
                List.of(new CraftAmount(firstOre, 1)),
                List.of(new CraftAmount(firstPlate, 1))),
            1);
        var secondStep = new CraftStep(
            "s2",
            pattern(
                "tinactory:second_plate",
                List.of(new CraftAmount(secondOre, 1)),
                List.of(new CraftAmount(secondPlate, 1))),
            1);
        executor.start(new CraftPlan(List.of(firstStep, secondStep)));

        executor.runCycle(64, 64);
        executor.runCycle(64, 64);

        assertEquals(List.of("start:s1", "start:s2", "done:s1", "done:s2"), events.events);
        assertEquals(JobState.IDLE, executor.state());
        assertEquals(1L, inventory.amountOf(firstPlate));
        assertEquals(1L, inventory.amountOf(secondPlate));
    }

    @Test
    void executorShouldBlockWhenPreconditionsMissing() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of());
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        executor.start(new CraftPlan(List.of(step)));

        executor.runCycle(64, 64);

        assertEquals(JobState.BLOCKED, executor.state());
        assertEquals(ExecutionError.INPUT_UNAVAILABLE, executor.error());
    }

    @Test
    void executorShouldCancelRunningJob() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        executor.start(new CraftPlan(List.of(step)));

        executor.cancel();
        executor.runCycle(64, 64);

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(ExecutionError.NONE, executor.error());
        assertEquals(0L, inventory.amountOf(plate));
    }

    @Test
    void executorShouldRetainIntermediateOutputsAndFlushFinalSurplus() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var part = TestStackKey.item("tinactory:part", "");
        var waste = TestStackKey.item("tinactory:waste", "");
        var gear = TestStackKey.item("tinactory:gear", "");
        var inventory = new FakeInventory(Map.of(ore, 1L));
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var firstStep = new CraftStep(
            "s1",
            pattern(
                "tinactory:part",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(part, 2), new CraftAmount(waste, 1))),
            1);
        var secondStep = new CraftStep(
            "s2",
            pattern(
                "tinactory:gear",
                List.of(new CraftAmount(part, 1)),
                List.of(new CraftAmount(gear, 1))),
            1);
        executor.start(new CraftPlan(List.of(firstStep, secondStep)));

        for (var i = 0; i < 16; i++) {
            executor.runCycle(64, 64);
        }

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(1L, inventory.amountOf(gear));
        assertEquals(1L, inventory.amountOf(part));
        assertEquals(1L, inventory.amountOf(waste));
        assertEquals(0, inventory.extractCallsByKey.getOrDefault(part, 0));
    }

    @Test
    void batchedStepShouldWaitForAllDeclaredOutputs() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var slag = TestStackKey.item("tinactory:slag", "");
        var inventory = new FakeInventory(Map.of(ore, 2L));
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern(
                "tinactory:plate",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(plate, 1), new CraftAmount(slag, 1))),
            2);
        executor.start(new CraftPlan(List.of(step)));

        executor.runCycle(2, 2);
        executor.runCycle(2, 2);

        assertEquals(JobState.RUNNING, executor.state());
        assertEquals(0L, inventory.amountOf(plate));
        assertEquals(0L, inventory.amountOf(slag));

        for (var i = 0; i < 8; i++) {
            executor.runCycle(64, 64);
        }

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(2L, inventory.amountOf(plate));
        assertEquals(2L, inventory.amountOf(slag));
    }

    @Test
    void fluidOutputsShouldUseFluidBandwidth() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var steam = TestStackKey.fluid("tinactory:steam", "");
        var inventory = new FakeInventory(Map.of(ore, 1L));
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern(
                "tinactory:steam",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(steam, 1000))),
            1);
        executor.start(new CraftPlan(List.of(step)));

        executor.runCycle(1, 250);
        executor.runCycle(1, 250);
        executor.runCycle(1, 250);

        assertEquals(JobState.RUNNING, executor.state());
        assertEquals(0L, inventory.amountOf(steam));

        executor.runCycle(1, 250);
        executor.runCycle(1, 250);
        executor.runCycle(1, 250);
        executor.runCycle(1, 250);
        executor.runCycle(1, 250);
        executor.runCycle(1, 250);

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(1000L, inventory.amountOf(steam));
    }

    @Test
    void batchedStepShouldRetainOnlyRequiredIntermediateOutputs() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var part = TestStackKey.item("tinactory:part", "");
        var scrap = TestStackKey.item("tinactory:scrap", "");
        var gear = TestStackKey.item("tinactory:gear", "");
        var inventory = new FakeInventory(Map.of(ore, 2L));
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var firstStep = new CraftStep(
            "s1",
            pattern(
                "tinactory:part",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(part, 2), new CraftAmount(scrap, 1))),
            2);
        var secondStep = new CraftStep(
            "s2",
            pattern(
                "tinactory:gear",
                List.of(new CraftAmount(part, 3)),
                List.of(new CraftAmount(gear, 1))),
            1);
        executor.start(new CraftPlan(List.of(firstStep, secondStep)));

        for (var i = 0; i < 16; i++) {
            executor.runCycle(64, 64);
        }

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(1L, inventory.amountOf(gear));
        assertEquals(1L, inventory.amountOf(part));
        assertEquals(2L, inventory.amountOf(scrap));
        assertEquals(0, inventory.extractCallsByKey.getOrDefault(part, 0));
    }

    @Test
    void batchedMachineLossShouldBlockUntilScheduledRunOutputsRecovered() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of(ore, 2L));
        var firstLease = new TestMachineLease(Map.of(ore, 1L), Map.of());
        var secondLease = new TestMachineLease(Map.of(ore, 1L), Map.of(plate, 2L));
        var executor = new CraftExecutor(
            inventory,
            new SequenceAllocator(List.of(firstLease, secondLease)),
            new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ore, 1)), List.of(new CraftAmount(plate, 1))),
            2);
        executor.start(new CraftPlan(List.of(step)));

        executor.runCycle(2, 1);
        firstLease.setValid(false);
        executor.runCycle(1, 1);

        assertEquals(JobState.BLOCKED, executor.state());
        assertEquals(ExecutionError.MACHINE_UNAVAILABLE, executor.error());
        assertEquals(0L, inventory.amountOf(plate));
    }

    @Test
    void batchedMachineLossShouldReassignWhenScheduledRunOutputsRecovered() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of(ore, 2L));
        var firstLease = new TestMachineLease(Map.of(ore, 1L), Map.of(plate, 1L));
        var secondLease = new TestMachineLease(Map.of(ore, 1L), Map.of(plate, 1L));
        var executor = new CraftExecutor(
            inventory,
            new SequenceAllocator(List.of(firstLease, secondLease)),
            new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ore, 1)), List.of(new CraftAmount(plate, 1))),
            2);
        executor.start(new CraftPlan(List.of(step)));

        executor.runCycle(2, 2);
        firstLease.setValid(false);
        for (var i = 0; i < 8; i++) {
            executor.runCycle(64, 64);
        }

        assertEquals(JobState.IDLE, executor.state());
        assertEquals(2L, inventory.amountOf(plate));
    }

    @Test
    void executorShouldExposeCheapExecutionStatus() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var executor = new CraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);

        executor.start(new CraftPlan(List.of(step)));

        assertEquals(JobState.RUNNING, executor.state());
        assertEquals(ExecutionPhase.RESERVING, executor.snapshot().phase());
        assertEquals(ExecutionError.NONE, executor.error());
        assertEquals(0, executor.completedSteps());
        assertEquals(1, executor.totalSteps());
    }

    @Test
    void targetedAllocationShouldNotUseNormalAllocationFallback() {
        var step = new CraftStep(
            "s1",
            pattern(
                "tinactory:plate",
                List.of(new CraftAmount(TestStackKey.item("tinactory:ore", ""), 1)),
                List.of(new CraftAmount(TestStackKey.item("tinactory:plate", ""), 1))),
            1);
        var lease = new TestMachineLease(Map.of(), Map.of());
        var allocator = TestMachineAllocator.single(lease);

        assertEquals(Optional.empty(), allocator.allocate(step, UUID.randomUUID()));
        assertEquals(0, allocator.normalCalls());
        assertEquals(0, lease.releaseCalls());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return TestAutocraftHelper.pattern(id, inputs, outputs);
    }

    private static final class FakeInventory implements IInventoryView {
        private final Map<IStackKey, Long> stock = new HashMap<>();
        private final Map<IStackKey, Integer> extractCallsByKey = new HashMap<>();
        private int insertCalls;

        private FakeInventory(Map<IStackKey, Long> initial) {
            stock.putAll(initial);
        }

        @Override
        public long amountOf(IStackKey key) {
            return stock.getOrDefault(key, 0L);
        }

        @Override
        public long extract(IStackKey key, long amount, boolean simulate) {
            var available = amountOf(key);
            var moved = Math.min(available, amount);
            if (!simulate && moved > 0L) {
                stock.put(key, available - moved);
                extractCallsByKey.merge(key, 1, Integer::sum);
            }
            return moved;
        }

        @Override
        public long insert(IStackKey key, long amount, boolean simulate) {
            var moved = Math.max(0L, amount);
            if (!simulate && moved > 0L) {
                insertCalls++;
                stock.merge(key, moved, Long::sum);
            }
            return moved;
        }
    }

    private static final class RecordingEvents implements IJobEvents {
        private final List<String> events = new ArrayList<>();

        @Override
        public void onStepStarted(CraftStep step) {
            events.add("start:" + step.stepId());
        }

        @Override
        public void onStepCompleted(CraftStep step) {
            events.add("done:" + step.stepId());
        }

        @Override
        public void onStepBlocked(CraftStep step, ExecutionError reason) {
            events.add("blocked:" + step.stepId() + ":" + reason.id);
        }
    }

    private static final class SimulatedAllocator implements IMachineAllocator {
        @Override
        public Optional<IMachineLease> allocate(CraftStep step, Set<UUID> excludedMachineIds) {
            return Optional.of(new SimulatedLease(step));
        }

        @Override
        public Optional<IMachineLease> allocate(CraftStep step, UUID machineId) {
            return Optional.empty();
        }
    }

    private static final class SequenceAllocator implements IMachineAllocator {
        private final List<IMachineLease> leases;
        private int index;

        private SequenceAllocator(List<IMachineLease> leases) {
            this.leases = List.copyOf(leases);
        }

        @Override
        public Optional<IMachineLease> allocate(CraftStep step, Set<UUID> excludedMachineIds) {
            var lease = leases.get(index);
            index++;
            return Optional.of(lease);
        }

        @Override
        public Optional<IMachineLease> allocate(CraftStep step, UUID machineId) {
            return leases.stream()
                .filter(lease -> lease.machineId().equals(machineId))
                .findFirst();
        }
    }

    private static final class SimulatedLease implements IMachineLease {
        private final UUID machineId = UUID.randomUUID();
        private final Map<IStackKey, Long> pushed = new HashMap<>();
        private final Map<IStackKey, Long> produced = new HashMap<>();
        private final Map<IStackKey, Long> requiredInputs = new HashMap<>();
        private final List<IMachineRoute> inputRoutes;
        private final List<IMachineRoute> outputRoutes;
        private boolean released;

        private SimulatedLease(CraftStep step) {
            for (var input : step.pattern().inputs()) {
                requiredInputs.put(input.key(), input.amount() * step.runs());
            }
            inputRoutes = step.pattern().inputs().stream().map(input -> (IMachineRoute) new IMachineRoute() {
                @Override
                public IStackKey key() {
                    return input.key();
                }

                @Override
                public PortDirection direction() {
                    return PortDirection.INPUT;
                }

                @Override
                public long transfer(long amount, boolean simulate) {
                    var moved = Math.max(0L, amount);
                    if (!simulate && moved > 0L) {
                        pushed.merge(input.key(), moved, Long::sum);
                        maybeProduce(step);
                    }
                    return moved;
                }
            }).toList();
            outputRoutes = step.pattern().outputs().stream()
                .map(output -> (IMachineRoute) new IMachineRoute() {
                    @Override
                    public IStackKey key() {
                        return output.key();
                    }

                    @Override
                    public PortDirection direction() {
                        return PortDirection.OUTPUT;
                    }

                    @Override
                    public long transfer(long amount, boolean simulate) {
                        var available = produced.getOrDefault(output.key(), 0L);
                        var moved = Math.min(Math.max(0L, amount), available);
                        if (!simulate && moved > 0L) {
                            produced.put(output.key(), available - moved);
                        }
                        return moved;
                    }
                })
                .toList();
        }

        private void maybeProduce(CraftStep step) {
            for (var input : requiredInputs.entrySet()) {
                if (pushed.getOrDefault(input.getKey(), 0L) < input.getValue()) {
                    return;
                }
            }
            for (var output : step.pattern().outputs()) {
                produced.merge(output.key(), output.amount() * step.runs(), Long::sum);
            }
            pushed.clear();
        }

        @Override
        public UUID machineId() {
            return machineId;
        }

        @Override
        public List<IMachineRoute> inputRoutes() {
            return inputRoutes;
        }

        @Override
        public List<IMachineRoute> outputRoutes() {
            return outputRoutes;
        }

        @Override
        public boolean isValid() {
            return !released;
        }

        @Override
        public void release() {
            released = true;
        }
    }
}
