package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineInputRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineOutputRoute;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftExecutorTest {
    @Test
    void executorShouldRunPlanSequentiallyThroughMachineRoutes() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var gear = CraftKey.item("tinactory:gear", "");

        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var events = new RecordingEvents();
        var allocator = new SimulatedAllocator();
        var executor = new SequentialCraftExecutor(inventory, allocator, events);
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
            executor.runCycle(64);
        }

        assertEquals(ExecutionState.COMPLETED, executor.state());
        assertEquals(1L, inventory.amountOf(gear));
        assertTrue(inventory.insertCalls > 0);
        assertEquals(List.of("start:s1", "done:s1", "start:s2", "done:s2"), events.events);
    }

    @Test
    void executorShouldBlockWhenPreconditionsMissing() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of());
        var executor = new SequentialCraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        executor.start(new CraftPlan(List.of(step)));

        executor.runCycle(64);

        assertEquals(ExecutionState.BLOCKED, executor.state());
        assertEquals("s1", executor.error().stepId());
    }

    @Test
    void executorShouldCancelRunningJob() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var executor = new SequentialCraftExecutor(inventory, new SimulatedAllocator(), new RecordingEvents());
        var step = new CraftStep(
            "s1",
            pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))),
            1);
        executor.start(new CraftPlan(List.of(step)));

        executor.cancel();
        executor.runCycle(64);

        assertEquals(ExecutionState.CANCELLED, executor.state());
        assertEquals(0L, inventory.amountOf(plate));
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs,
            new MachineRequirement(new ResourceLocation("tinactory", "machine"), 1, List.of()));
    }

    private static final class FakeInventory implements IInventoryView {
        private final Map<CraftKey, Long> stock = new HashMap<>();
        private int insertCalls;

        private FakeInventory(Map<CraftKey, Long> initial) {
            stock.putAll(initial);
        }

        @Override
        public long amountOf(CraftKey key) {
            return stock.getOrDefault(key, 0L);
        }

        @Override
        public long extract(CraftKey key, long amount, boolean simulate) {
            var available = amountOf(key);
            var moved = Math.min(available, amount);
            if (!simulate && moved > 0L) {
                stock.put(key, available - moved);
            }
            return moved;
        }

        @Override
        public long insert(CraftKey key, long amount, boolean simulate) {
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
        public void onStepBlocked(CraftStep step, String reason) {
            events.add("blocked:" + step.stepId() + ":" + reason);
        }
    }

    private static final class SimulatedAllocator implements IMachineAllocator {
        @Override
        public Optional<IMachineLease> allocate(CraftStep step) {
            return Optional.of(new SimulatedLease(step));
        }
    }

    private static final class SimulatedLease implements IMachineLease {
        private final UUID machineId = UUID.randomUUID();
        private final Map<CraftKey, Long> pushed = new HashMap<>();
        private final Map<CraftKey, Long> produced = new HashMap<>();
        private final Map<CraftKey, Long> requiredInputs = new HashMap<>();
        private final List<IMachineInputRoute> inputRoutes;
        private final List<IMachineOutputRoute> outputRoutes;
        private boolean released;

        private SimulatedLease(CraftStep step) {
            for (var input : step.pattern().inputs()) {
                requiredInputs.put(input.key(), input.amount() * step.runs());
            }
            inputRoutes = step.pattern().inputs().stream().map(input -> (IMachineInputRoute) new IMachineInputRoute() {
                @Override
                public CraftKey key() {
                    return input.key();
                }

                @Override
                public long push(long amount, boolean simulate) {
                    var moved = Math.max(0L, amount);
                    if (!simulate && moved > 0L) {
                        pushed.merge(input.key(), moved, Long::sum);
                        maybeProduce(step);
                    }
                    return moved;
                }
            }).toList();
            outputRoutes = step.pattern().outputs().stream()
                .map(output -> (IMachineOutputRoute) new IMachineOutputRoute() {
                    @Override
                    public CraftKey key() {
                        return output.key();
                    }

                    @Override
                    public long pull(long amount, boolean simulate) {
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
        public List<IMachineInputRoute> inputRoutes() {
            return inputRoutes;
        }

        @Override
        public List<IMachineOutputRoute> outputRoutes() {
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
