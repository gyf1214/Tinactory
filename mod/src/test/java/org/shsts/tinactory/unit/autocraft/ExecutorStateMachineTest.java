package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineInputRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineOutputRoute;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
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

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs,
            new MachineRequirement(new ResourceLocation("tinactory", "machine"), 1, List.of()));
    }

    private static final class MutableInventory implements IInventoryView {
        private final Map<CraftKey, Long> amounts = new HashMap<>();
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
                amounts.put(key, available - moved);
            }
            return moved;
        }

        @Override
        public long insert(CraftKey key, long amount, boolean simulate) {
            var moved = Math.max(0L, amount);
            if (!simulate && moved > 0L) {
                amounts.merge(key, moved, Long::sum);
            }
            return moved;
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
