package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineInputRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineOutputRoute;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJob;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.integration.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
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
import java.util.Optional;
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
        first.tick();
        var snapshot = first.snapshotRunning().orElseThrow();

        var restored = new AutocraftJobService(cpuId, planner, AutocraftCpuPersistenceTest::executor, List::of);
        restored.restoreRunning(snapshot);
        assertEquals(AutocraftJob.Status.RUNNING, restored.job(jobId).status());

        restored.tick();
        restored.tick();
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
        assertTrue(snapshot.runtimeSnapshot().nextStepIndex() >= 0);
    }

    @Test
    void serviceShouldPersistStepOutputRolesInSnapshot() {
        var cpuId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        var step = new CraftStep(
            "s1",
            new CraftPattern(
                "tinactory:s1",
                List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 2)),
                List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 2)),
                new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())),
            1,
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)));
        var plan = new CraftPlan(List.of(step));
        var planner = new FixedPlanner(PlanResult.success(plan));
        var service = new AutocraftJobService(cpuId, planner, AutocraftCpuPersistenceTest::executor, List::of);
        var codec = new PatternNbtCodec(new MachineConstraintRegistry());

        service.submit(List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)));
        service.tick();
        var serialized = service.serializeRunningSnapshot(codec).orElseThrow();
        var restored = new AutocraftJobService(cpuId, planner, AutocraftCpuPersistenceTest::executor, List::of);
        restored.restoreRunningSnapshot(serialized, codec);

        var restoredStep = restored.snapshotRunning().orElseThrow().plan().steps().get(0);
        assertEquals(List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            restoredStep.requiredIntermediateOutputs());
        assertEquals(List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            restoredStep.requiredFinalOutputs());
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
        public long extract(CraftKey key, long amount, boolean simulate) {
            var current = amountOf(key);
            var moved = Math.min(current, amount);
            if (!simulate) {
                amounts.put(key, current - moved);
            }
            return moved;
        }

        @Override
        public long insert(CraftKey key, long amount, boolean simulate) {
            var moved = Math.max(0L, amount);
            if (!simulate) {
                amounts.put(key, amountOf(key) + moved);
            }
            return moved;
        }
    }

    private static final class AlwaysMachineAllocator implements IMachineAllocator {
        @Override
        public Optional<IMachineLease> allocate(CraftStep step) {
            return Optional.of(new IMachineLease() {
                @Override
                public UUID machineId() {
                    return UUID.fromString("11111111-1111-1111-1111-111111111111");
                }

                @Override
                public List<IMachineInputRoute> inputRoutes() {
                    return step.pattern().inputs().stream().map(input -> (IMachineInputRoute) new IMachineInputRoute() {
                        @Override
                        public CraftKey key() {
                            return input.key();
                        }

                        @Override
                        public long push(long amount, boolean simulate) {
                            return amount;
                        }
                    }).toList();
                }

                @Override
                public List<IMachineOutputRoute> outputRoutes() {
                    return step.pattern().outputs().stream()
                        .map(output -> (IMachineOutputRoute) new IMachineOutputRoute() {
                            @Override
                            public CraftKey key() {
                                return output.key();
                            }

                            @Override
                            public long pull(long amount, boolean simulate) {
                                return amount;
                            }
                        })
                        .toList();
                }

                @Override
                public boolean isValid() {
                    return true;
                }

                @Override
                public void release() {
                }
            });
        }
    }

    private static final class NoOpEvents implements IJobEvents {
    }
}
