package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJob;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftCpuPersistenceTest {
    @Test
    void serviceShouldResumeFromRunningSnapshot() {
        var cpuId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var target = new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1);
        var plan = new CraftPlan(List.of(step("s1"), step("s2")));
        var planner = new FixedPlanner(PlanResult.success(plan));

        var first = new AutocraftJobService(cpuId, planner, AutocraftCpuPersistenceTest::executor, List::of);
        var jobId = first.submit(List.of(target));
        first.tick();
        first.tick();
        var snapshot = first.snapshotRunning().orElseThrow();

        var restored = new AutocraftJobService(cpuId, planner, AutocraftCpuPersistenceTest::executor, List::of);
        restored.restoreRunning(snapshot);
        assertEquals(AutocraftJob.Status.RUNNING, restored.job(jobId).status());

        restored.tick();
        assertEquals(AutocraftJob.Status.DONE, restored.job(jobId).status());
    }

    @Test
    void serviceShouldKeepCpuIdInSnapshot() {
        var cpuId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        var plan = new CraftPlan(List.of(step("s1")));
        var service = new AutocraftJobService(cpuId, new FixedPlanner(PlanResult.success(plan)),
            AutocraftCpuPersistenceTest::executor, List::of);

        service.submit(List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)));
        service.tick();
        var snapshot = service.snapshotRunning().orElseThrow();

        assertEquals(cpuId, snapshot.cpuId());
        assertTrue(snapshot.nextStepIndex() >= 0);
    }

    private static CraftStep step(String id) {
        return new CraftStep(id, new CraftPattern(
            "tinactory:" + id,
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())), 1);
    }

    private static SequentialCraftExecutor executor() {
        return new SequentialCraftExecutor(new NoOpInventory(), new AlwaysMachineAllocator(), new NoOpEvents());
    }

    private record FixedPlanner(PlanResult result) implements ICraftPlanner {
        @Override
        public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
            return result;
        }
    }

    private static final class NoOpInventory implements IInventoryView {
        private final Map<CraftKey, Long> amounts = new HashMap<>();

        @Override
        public long amountOf(CraftKey key) {
            return amounts.computeIfAbsent(key, $ -> 64L);
        }

        @Override
        public boolean consume(CraftKey key, long amount) {
            var current = amountOf(key);
            if (current < amount) {
                return false;
            }
            amounts.put(key, current - amount);
            return true;
        }

        @Override
        public void produce(CraftKey key, long amount) {
            amounts.put(key, amountOf(key) + amount);
        }
    }

    private static final class AlwaysMachineAllocator implements IMachineAllocator {
        @Override
        public boolean canRun(MachineRequirement requirement) {
            return true;
        }
    }

    private static final class NoOpEvents implements IJobEvents {}
}
