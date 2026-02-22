package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class CraftExecutorTest {
    @Test
    void executorShouldRunPlanSequentially() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var gear = CraftKey.item("tinactory:gear", "");

        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var events = new RecordingEvents();
        var executor = new SequentialCraftExecutor(inventory, requirement -> true, events);
        executor.start(new CraftPlan(List.of(
            new CraftStep("s1", pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))), 1),
            new CraftStep("s2", pattern("tinactory:gear", List.of(new CraftAmount(plate, 1)), List.of(new CraftAmount(gear, 1))), 1)
        )));

        executor.tick();
        executor.tick();

        assertEquals(ExecutionState.COMPLETED, executor.state());
        assertEquals(1L, inventory.amountOf(gear));
        assertEquals(List.of("start:s1", "done:s1", "start:s2", "done:s2"), events.events);
    }

    @Test
    void executorShouldBlockWhenPreconditionsMissing() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of());
        var executor = new SequentialCraftExecutor(inventory, requirement -> true, new RecordingEvents());
        executor.start(new CraftPlan(List.of(
            new CraftStep("s1", pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))), 1)
        )));

        executor.tick();

        assertEquals(ExecutionState.BLOCKED, executor.state());
        assertEquals("s1", executor.error().stepId());
    }

    @Test
    void executorShouldCancelRunningJob() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var inventory = new FakeInventory(Map.of(ingot, 2L));
        var executor = new SequentialCraftExecutor(inventory, requirement -> true, new RecordingEvents());
        executor.start(new CraftPlan(List.of(
            new CraftStep("s1", pattern("tinactory:plate", List.of(new CraftAmount(ingot, 2)), List.of(new CraftAmount(plate, 1))), 1)
        )));

        executor.cancel();

        assertEquals(ExecutionState.CANCELLED, executor.state());
        executor.tick();
        assertEquals(0L, inventory.amountOf(plate));
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs, new MachineRequirement("tinactory:machine", 1, List.of()));
    }

    private static final class FakeInventory implements IInventoryView {
        private final Map<CraftKey, Long> stock = new HashMap<>();

        private FakeInventory(Map<CraftKey, Long> initial) {
            stock.putAll(initial);
        }

        @Override
        public long amountOf(CraftKey key) {
            return stock.getOrDefault(key, 0L);
        }

        @Override
        public boolean consume(CraftKey key, long amount) {
            var available = amountOf(key);
            if (available < amount) {
                return false;
            }
            stock.put(key, available - amount);
            return true;
        }

        @Override
        public void produce(CraftKey key, long amount) {
            stock.merge(key, amount, Long::sum);
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
}
