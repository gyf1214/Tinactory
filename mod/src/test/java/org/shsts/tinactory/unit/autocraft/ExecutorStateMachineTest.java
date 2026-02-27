package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineInputRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineOutputRoute;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ExecutorRuntimeSnapshot;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutorStateMachineTest {
    @Test
    void reservationShouldConsumeBufferedInputsBeforeNetworkExtraction() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var step = new CraftStep(
            "s1",
            pattern("tinactory:press", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        var inventory = new MutableInventory(Map.of(ingot, 1L));
        var executor = new SequentialCraftExecutor(
            inventory,
            $ -> Optional.of(new RouteLease(Map.of(), Map.of(), true)),
            new NoOpEvents());
        var snapshot = new ExecutorRuntimeSnapshot(
            ExecutionState.RUNNING,
            ExecutionDetails.Phase.RUN_STEP,
            null,
            null,
            null,
            0,
            Map.of(ingot, 1L),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            null);

        executor.start(new CraftPlan(List.of(step)), 0, snapshot);
        executor.runCycle(0);

        assertEquals(ExecutionState.RUNNING, executor.state());
        assertEquals(0L, inventory.amountOf(ingot));
        assertEquals(2L, executor.details().stepBuffer().getOrDefault(ingot, 0L));
    }

    @Test
    void reservationShouldRollbackWhenBufferedFirstNetworkExtractionIsPartial() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var step = new CraftStep(
            "s1",
            pattern("tinactory:press", List.of(new CraftAmount(ingot, 3)), List.of(new CraftAmount(plate, 1))),
            1);
        var inventory = new MutableInventory(Map.of(ingot, 2L));
        inventory.forcedActualExtract.put(ingot, 1L);
        var executor = new SequentialCraftExecutor(
            inventory,
            $ -> Optional.of(new RouteLease(Map.of(), Map.of(), true)),
            new NoOpEvents());
        var snapshot = new ExecutorRuntimeSnapshot(
            ExecutionState.RUNNING,
            ExecutionDetails.Phase.RUN_STEP,
            null,
            null,
            null,
            0,
            Map.of(ingot, 1L),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            null);

        executor.start(new CraftPlan(List.of(step)), 0, snapshot);
        executor.runCycle(0);

        assertEquals(ExecutionState.BLOCKED, executor.state());
        assertEquals(ExecutionError.Code.INPUT_UNAVAILABLE, executor.error().code());
        assertEquals(2L, inventory.amountOf(ingot));
        assertEquals(1L, executor.details().stepBuffer().getOrDefault(ingot, 0L));
    }

    @Test
    void stepCompletionShouldUseRequiredOutputsOnly() {
        var ore = CraftKey.item("tinactory:ore", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var waste = CraftKey.item("tinactory:waste", "");
        var step = new CraftStep(
            "s1",
            pattern(
                "tinactory:smelt",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(plate, 1), new CraftAmount(waste, 1))),
            1,
            List.of(new CraftAmount(plate, 1)));

        var inventory = new MutableInventory(Map.of(ore, 1L));
        var allocator = new SingleLeaseAllocator(new RouteLease(
            Map.of(ore, 1L),
            Map.of(plate, 1L),
            true));
        var executor = new SequentialCraftExecutor(inventory, allocator, new NoOpEvents());

        executor.start(new CraftPlan(List.of(step)));
        executor.runCycle(4);
        executor.runCycle(4);
        executor.runCycle(4);
        executor.runCycle(4);

        assertEquals(ExecutionState.COMPLETED, executor.state());
        assertEquals(1L, inventory.amountOf(plate));
        assertEquals(0L, inventory.amountOf(waste));
    }

    @Test
    void reservationShouldRollbackWhenActualExtractionFails() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var coal = CraftKey.item("tinactory:coal", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var step = new CraftStep(
            "s1",
            pattern(
                "tinactory:press",
                List.of(new CraftAmount(ingot, 1), new CraftAmount(coal, 1)),
                List.of(new CraftAmount(plate, 1))),
            1);
        var inventory = new MutableInventory(Map.of(ingot, 1L, coal, 1L));
        inventory.failSecondExtract = true;
        var executor = new SequentialCraftExecutor(
            inventory,
            $ -> Optional.of(new RouteLease(Map.of(), Map.of(), true)),
            new NoOpEvents());

        executor.start(new CraftPlan(List.of(step)));
        executor.runCycle(4);

        assertEquals(ExecutionState.BLOCKED, executor.state());
        assertEquals(1L, inventory.amountOf(ingot));
        assertEquals(1L, inventory.amountOf(coal));
        assertEquals(ExecutionError.Code.INPUT_UNAVAILABLE, executor.error().code());
    }

    @Test
    void machineLossShouldBlockWhenReassignmentGateFails() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var step = new CraftStep(
            "s1",
            pattern("tinactory:press", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        var firstLease = new RouteLease(Map.of(ingot, 1L), Map.of(), false);
        var secondLease = new RouteLease(Map.of(ingot, 1L), Map.of(plate, 1L), true);
        var allocator = new SequenceAllocator(List.of(firstLease, secondLease));
        var executor = new SequentialCraftExecutor(
            new MutableInventory(Map.of(ingot, 2L)),
            allocator,
            new NoOpEvents());

        executor.start(new CraftPlan(List.of(step)));
        executor.runCycle(1);
        firstLease.valid = false;
        executor.runCycle(1);

        assertEquals(ExecutionState.BLOCKED, executor.state());
        assertEquals(ExecutionError.Code.MACHINE_REASSIGNMENT_BLOCKED, executor.error().code());
        assertEquals(ExecutionDetails.Phase.RUN_STEP, executor.details().phase());
    }

    @Test
    void reservationShouldNotBlockWhenOnlyBufferLimitWouldOverflow() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var step = new CraftStep(
            "s1",
            pattern("tinactory:press", List.of(new CraftAmount(ingot, 3)), List.of(new CraftAmount(plate, 1))),
            1);
        var lease = new RouteLease(Map.of(ingot, 3L), Map.of(plate, 1L), true);
        var executor = new SequentialCraftExecutor(
            new MutableInventory(Map.of(ingot, 3L)),
            $ -> Optional.of(lease),
            new NoOpEvents());

        executor.start(new CraftPlan(List.of(step)));
        executor.runCycle(4);
        executor.runCycle(4);
        executor.runCycle(4);
        executor.runCycle(4);

        assertEquals(ExecutionState.COMPLETED, executor.state());
    }

    @Test
    void stepBoundaryShouldKeepIntermediateOutputsBufferedForDownstreamStep() {
        var ore = CraftKey.item("tinactory:ore", "");
        var part = CraftKey.item("tinactory:part", "");
        var gear = CraftKey.item("tinactory:gear", "");
        var firstStep = new CraftStep(
            "s1",
            pattern(
                "tinactory:part",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(part, 1))),
            1,
            List.of(new CraftAmount(part, 1)),
            List.of());
        var secondStep = new CraftStep(
            "s2",
            pattern(
                "tinactory:gear",
                List.of(new CraftAmount(part, 1)),
                List.of(new CraftAmount(gear, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(gear, 1)));
        var firstLease = new RouteLease(Map.of(ore, 1L), Map.of(part, 1L), true);
        var allocator = new IMachineAllocator() {
            private int call;

            @Override
            public Optional<IMachineLease> allocate(CraftStep step) {
                call++;
                if (call == 1) {
                    return Optional.of(firstLease);
                }
                return Optional.empty();
            }
        };
        var inventory = new MutableInventory(Map.of(ore, 1L));
        var executor = new SequentialCraftExecutor(inventory, allocator, new NoOpEvents());

        executor.start(new CraftPlan(List.of(firstStep, secondStep)));
        executor.runCycle(64);
        executor.runCycle(64);
        executor.runCycle(64);
        executor.runCycle(64);

        assertEquals(ExecutionState.BLOCKED, executor.state());
        assertEquals(ExecutionError.Code.MACHINE_UNAVAILABLE, executor.error().code());
        assertEquals(0L, inventory.amountOf(part));
    }

    @Test
    void branchingDownstreamStepsShouldConsumeAllBufferedIntermediates() {
        var ore = CraftKey.item("tinactory:ore", "");
        var part = CraftKey.item("tinactory:part", "");
        var machineA = CraftKey.item("tinactory:machine_a", "");
        var machineB = CraftKey.item("tinactory:machine_b", "");

        var firstStep = new CraftStep(
            "s1",
            pattern("tinactory:part", List.of(new CraftAmount(ore, 1)), List.of(new CraftAmount(part, 2))),
            1,
            List.of(new CraftAmount(part, 2)),
            List.of());
        var secondStep = new CraftStep(
            "s2",
            pattern("tinactory:machine_a", List.of(new CraftAmount(part, 1)), List.of(new CraftAmount(machineA, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(machineA, 1)));
        var thirdStep = new CraftStep(
            "s3",
            pattern("tinactory:machine_b", List.of(new CraftAmount(part, 1)), List.of(new CraftAmount(machineB, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(machineB, 1)));

        var firstLease = new RouteLease(Map.of(ore, 1L), Map.of(part, 2L), true);
        var secondLease = new RouteLease(Map.of(part, 1L), Map.of(machineA, 1L), true);
        var thirdLease = new RouteLease(Map.of(part, 1L), Map.of(machineB, 1L), true);
        var allocator = new SequenceAllocator(List.of(firstLease, secondLease, thirdLease));
        var inventory = new MutableInventory(Map.of(ore, 1L));
        var executor = new SequentialCraftExecutor(inventory, allocator, new NoOpEvents());

        executor.start(new CraftPlan(List.of(firstStep, secondStep, thirdStep)));
        for (int i = 0; i < 12; i++) {
            executor.runCycle(64);
        }

        assertEquals(ExecutionState.COMPLETED, executor.state());
        assertEquals(1L, inventory.amountOf(machineA));
        assertEquals(1L, inventory.amountOf(machineB));
        assertEquals(0L, inventory.amountOf(part));
    }

    @Test
    void fanInStepsShouldAccumulateBufferedIntermediatesForSingleConsumerStep() {
        var ore = CraftKey.item("tinactory:ore", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var part = CraftKey.item("tinactory:part", "");
        var machine = CraftKey.item("tinactory:machine", "");

        var firstStep = new CraftStep(
            "s1",
            pattern("tinactory:part_from_ore", List.of(new CraftAmount(ore, 1)), List.of(new CraftAmount(part, 1))),
            1,
            List.of(new CraftAmount(part, 1)),
            List.of());
        var secondStep = new CraftStep(
            "s2",
            pattern(
                "tinactory:part_from_plate",
                List.of(new CraftAmount(plate, 1)),
                List.of(new CraftAmount(part, 1))),
            1,
            List.of(new CraftAmount(part, 1)),
            List.of());
        var thirdStep = new CraftStep(
            "s3",
            pattern(
                "tinactory:machine_from_part",
                List.of(new CraftAmount(part, 2)),
                List.of(new CraftAmount(machine, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(machine, 1)));

        var firstLease = new RouteLease(Map.of(ore, 1L), Map.of(part, 1L), true);
        var secondLease = new RouteLease(Map.of(plate, 1L), Map.of(part, 1L), true);
        var thirdLease = new RouteLease(Map.of(part, 2L), Map.of(machine, 1L), true);
        var allocator = new SequenceAllocator(List.of(firstLease, secondLease, thirdLease));
        var inventory = new MutableInventory(Map.of(ore, 1L, plate, 1L));
        var executor = new SequentialCraftExecutor(inventory, allocator, new NoOpEvents());

        executor.start(new CraftPlan(List.of(firstStep, secondStep, thirdStep)));
        for (int i = 0; i < 12; i++) {
            executor.runCycle(64);
        }

        assertEquals(ExecutionState.COMPLETED, executor.state());
        assertEquals(1L, inventory.amountOf(machine));
        assertEquals(0L, inventory.amountOf(part));
        assertEquals(0L, inventory.actualInserted(part));
        assertEquals(0L, inventory.actualExtracted(part));
    }

    @Test
    void stepBoundaryFlushShouldKeepCarriedBufferForSameKeyOutput() {
        var ore = CraftKey.item("tinactory:ore", "");
        var part = CraftKey.item("tinactory:part", "");
        var machine = CraftKey.item("tinactory:machine", "");

        var firstStep = new CraftStep(
            "s1",
            pattern("tinactory:part_from_ore", List.of(new CraftAmount(ore, 1)), List.of(new CraftAmount(part, 2))),
            1,
            List.of(new CraftAmount(part, 1)),
            List.of());
        var secondStep = new CraftStep(
            "s2",
            pattern(
                "tinactory:machine_from_part",
                List.of(new CraftAmount(part, 2)),
                List.of(new CraftAmount(machine, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(machine, 1)));

        var firstLease = new RouteLease(Map.of(ore, 1L), Map.of(part, 2L), true);
        var secondLease = new RouteLease(Map.of(part, 2L), Map.of(machine, 1L), true);
        var allocator = new SequenceAllocator(List.of(firstLease, secondLease));
        var inventory = new MutableInventory(Map.of(ore, 1L));
        var executor = new SequentialCraftExecutor(inventory, allocator, new NoOpEvents());
        var snapshot = new ExecutorRuntimeSnapshot(
            ExecutionState.RUNNING,
            ExecutionDetails.Phase.RUN_STEP,
            null,
            null,
            null,
            0,
            Map.of(part, 1L),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            null);

        executor.start(new CraftPlan(List.of(firstStep, secondStep)), 0, snapshot);
        executor.runCycle(64);
        executor.runCycle(64);
        executor.runCycle(64);

        for (int i = 0; i < 6; i++) {
            executor.runCycle(64);
        }

        assertEquals(ExecutionState.COMPLETED, executor.state());
        assertEquals(1L, inventory.amountOf(machine));
        assertEquals(1L, inventory.amountOf(part));
        assertEquals(0L, inventory.actualExtracted(part));
    }

    @Test
    void stepBoundaryFlushShouldIgnoreUnrelatedBufferedKeys() {
        var ore = CraftKey.item("tinactory:ore", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var gear = CraftKey.item("tinactory:gear", "");
        var carry = CraftKey.item("tinactory:carry", "");
        var firstStep = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ore, 1)), List.of(new CraftAmount(plate, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(plate, 1)));
        var secondStep = new CraftStep(
            "s2",
            pattern("tinactory:gear", List.of(new CraftAmount(plate, 1)), List.of(new CraftAmount(gear, 1))),
            1,
            List.of(),
            List.of(new CraftAmount(gear, 1)));
        var firstLease = new RouteLease(Map.of(ore, 1L), Map.of(plate, 1L), true);
        var secondLease = new RouteLease(Map.of(plate, 1L), Map.of(), true);
        var allocator = new SequenceAllocator(List.of(firstLease, secondLease));
        var inventory = new MutableInventory(Map.of(ore, 1L));
        inventory.rejectInsertKeys.add(carry);
        var executor = new SequentialCraftExecutor(inventory, allocator, new NoOpEvents());
        var snapshot = new ExecutorRuntimeSnapshot(
            ExecutionState.RUNNING,
            ExecutionDetails.Phase.RUN_STEP,
            null,
            null,
            null,
            0,
            Map.of(carry, 1L),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            null);

        executor.start(new CraftPlan(List.of(firstStep, secondStep)), 0, snapshot);
        executor.runCycle(64);
        executor.runCycle(64);
        executor.runCycle(64);

        assertEquals(ExecutionState.RUNNING, executor.state());
        assertEquals(ExecutionDetails.Phase.RUN_STEP, executor.details().phase());
        assertEquals(1, executor.details().nextStepIndex());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs,
            new MachineRequirement(new ResourceLocation("tinactory", "machine"), 1, List.of()));
    }

    private static final class MutableInventory implements IInventoryView {
        private final Map<CraftKey, Long> amounts = new HashMap<>();
        private final java.util.Set<CraftKey> rejectInsertKeys = new java.util.HashSet<>();
        private final Map<CraftKey, Long> forcedActualExtract = new HashMap<>();
        private final Map<CraftKey, Long> actualExtracted = new HashMap<>();
        private final Map<CraftKey, Long> actualInserted = new HashMap<>();
        private int extractCount;
        private boolean failSecondExtract;

        private MutableInventory(Map<CraftKey, Long> initial) {
            amounts.putAll(initial);
        }

        @Override
        public long amountOf(CraftKey key) {
            return amounts.getOrDefault(key, 0L);
        }

        @Override
        public long extract(CraftKey key, long amount, boolean simulate) {
            var available = amountOf(key);
            var moved = Math.min(available, amount);
            if (!simulate) {
                extractCount++;
                if (failSecondExtract && extractCount == 2) {
                    return 0L;
                }
                moved = Math.min(moved, forcedActualExtract.getOrDefault(key, moved));
                amounts.put(key, available - moved);
                if (moved > 0L) {
                    actualExtracted.merge(key, moved, Long::sum);
                }
            }
            return moved;
        }

        @Override
        public long insert(CraftKey key, long amount, boolean simulate) {
            var moved = Math.max(0L, amount);
            if (rejectInsertKeys.contains(key)) {
                return 0L;
            }
            if (!simulate && moved > 0L) {
                amounts.merge(key, moved, Long::sum);
                actualInserted.merge(key, moved, Long::sum);
            }
            return moved;
        }

        private long actualExtracted(CraftKey key) {
            return actualExtracted.getOrDefault(key, 0L);
        }

        private long actualInserted(CraftKey key) {
            return actualInserted.getOrDefault(key, 0L);
        }
    }

    private record NoOpEvents() implements IJobEvents {}

    private static final class SingleLeaseAllocator implements IMachineAllocator {
        private final IMachineLease lease;

        private SingleLeaseAllocator(IMachineLease lease) {
            this.lease = lease;
        }

        @Override
        public Optional<IMachineLease> allocate(CraftStep step) {
            return Optional.of(lease);
        }
    }

    private static final class SequenceAllocator implements IMachineAllocator {
        private final List<IMachineLease> leases;
        private int next;

        private SequenceAllocator(List<IMachineLease> leases) {
            this.leases = leases;
        }

        @Override
        public Optional<IMachineLease> allocate(CraftStep step) {
            if (next >= leases.size()) {
                return Optional.empty();
            }
            return Optional.of(leases.get(next++));
        }
    }

    private static final class RouteLease implements IMachineLease {
        private final UUID machineId = UUID.randomUUID();
        private final Map<CraftKey, Long> inputMove;
        private final Map<CraftKey, Long> outputMove;
        private final List<IMachineInputRoute> inputs;
        private final List<IMachineOutputRoute> outputs;
        private boolean released;
        private boolean valid;

        private RouteLease(Map<CraftKey, Long> inputMove, Map<CraftKey, Long> outputMove, boolean valid) {
            this.inputMove = inputMove;
            this.outputMove = new HashMap<>(outputMove);
            this.valid = valid;
            inputs = inputMove.keySet().stream().map(key -> (IMachineInputRoute) new IMachineInputRoute() {
                @Override
                public CraftKey key() {
                    return key;
                }

                @Override
                public long push(long amount, boolean simulate) {
                    return Math.min(amount, inputMove.getOrDefault(key, 0L));
                }
            }).toList();
            outputs = outputMove.keySet().stream().map(key -> (IMachineOutputRoute) new IMachineOutputRoute() {
                @Override
                public CraftKey key() {
                    return key;
                }

                @Override
                public long pull(long amount, boolean simulate) {
                    var available = RouteLease.this.outputMove.getOrDefault(key, 0L);
                    var moved = Math.min(available, amount);
                    if (!simulate) {
                        RouteLease.this.outputMove.put(key, available - moved);
                    }
                    return moved;
                }
            }).toList();
        }

        @Override
        public UUID machineId() {
            return machineId;
        }

        @Override
        public List<IMachineInputRoute> inputRoutes() {
            return inputs;
        }

        @Override
        public List<IMachineOutputRoute> outputRoutes() {
            return outputs;
        }

        @Override
        public boolean isValid() {
            return !released && valid;
        }

        @Override
        public void release() {
            released = true;
        }
    }
}
