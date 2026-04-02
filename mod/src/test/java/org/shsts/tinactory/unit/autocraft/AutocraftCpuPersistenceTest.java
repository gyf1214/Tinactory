package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.integration.autocraft.MachineConstraintCodecHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftCpuPersistenceTest {
    @Test
    void serviceShouldResumeFromRunningSnapshot() {
        var target = new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1);
        var plan = new CraftPlan(List.of(step("s1"), step("s2")));

        var first = new AutocraftJobService(executor());
        var jobId = first.submitPrepared(List.of(target), plan);
        first.tick();
        var snapshot = first.snapshotRunning().orElseThrow();

        var restored = new AutocraftJobService(executor());
        restored.restoreRunning(snapshot);
        assertEquals(JobState.RUNNING, restored.getJob().orElseThrow().execution().state());
        assertEquals(jobId, restored.getJob().orElseThrow().jobId());

        while (restored.getJob().orElseThrow().execution().state() == JobState.RUNNING ||
            restored.getJob().orElseThrow().execution().state() == JobState.BLOCKED) {
            restored.tick();
        }
        assertEquals(JobState.COMPLETED, restored.getJob().orElseThrow().execution().state());
    }

    @Test
    void serviceShouldSerializeExecutionSnapshotWithoutSeparatePlanOrRuntimeFields() {
        var plan = new CraftPlan(List.of(step("s1")));
        var service = new AutocraftJobService(executor());
        var codec = new PatternNbtCodec(MachineConstraintCodecHelper.CODEC, TestIngredientKey.CODEC);

        service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)), plan);
        service.tick();
        var snapshot = service.serializeRunningSnapshot(codec).orElseThrow();

        assertTrue(snapshot.contains("execution"));
        assertFalse(snapshot.contains("plan"));
        assertFalse(snapshot.contains("runtime"));
        assertEquals("NONE", snapshot.getCompound("execution").getCompound("error").getString("value"));
    }

    @Test
    void serviceShouldPersistStepOutputRolesInExecutionPlan() {
        var step = new CraftStep(
            "s1",
            new CraftPattern(
                "tinactory:s1",
                List.of(new CraftAmount(TestIngredientKey.item("minecraft:cobblestone", ""), 2)),
                List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 2)),
                new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())),
            1,
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)));
        var plan = new CraftPlan(List.of(step));
        var service = new AutocraftJobService(executor());
        var codec = new PatternNbtCodec(MachineConstraintCodecHelper.CODEC, TestIngredientKey.CODEC);

        service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)), plan);
        service.tick();
        var serialized = service.serializeRunningSnapshot(codec).orElseThrow();
        var restored = new AutocraftJobService(executor());
        restored.restoreRunningSnapshot(serialized, codec);

        var restoredStep = restored.snapshotRunning().orElseThrow().execution().plan().steps().get(0);
        assertEquals(List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)),
            restoredStep.requiredIntermediateOutputs());
        assertEquals(List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)),
            restoredStep.requiredFinalOutputs());
    }

    @Test
    void tickShouldReportDirtyStateChangesOnlyWhenJobIsStillBusy() {
        var plan = new CraftPlan(List.of(step("s1")));
        var service = new AutocraftJobService(executor());
        service.submitPrepared(List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)), plan);

        assertTrue(service.tick());
        assertTrue(service.tick());
        assertFalse(service.tick());
        assertFalse(service.tick());
    }

    private static CraftStep step(String id) {
        return new CraftStep(id, new CraftPattern(
            "tinactory:" + id,
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of())), 1);
    }

    private static SequentialCraftExecutor executor() {
        return new SequentialCraftExecutor(new NoOpInventory(), new AlwaysMachineAllocator(), new NoOpEvents());
    }

    private static final class NoOpInventory implements IInventoryView {
        private final Map<IIngredientKey, Long> amounts = new HashMap<>();

        @Override
        public long amountOf(IIngredientKey key) {
            return amounts.computeIfAbsent(key, $ -> 64L);
        }

        @Override
        public long extract(IIngredientKey key, long amount, boolean simulate) {
            var current = amountOf(key);
            var moved = Math.min(current, amount);
            if (!simulate) {
                amounts.put(key, current - moved);
            }
            return moved;
        }

        @Override
        public long insert(IIngredientKey key, long amount, boolean simulate) {
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
                public List<IMachineRoute> inputRoutes() {
                    return step.pattern().inputs().stream().map(input -> (IMachineRoute) new IMachineRoute() {
                        @Override
                        public IIngredientKey key() {
                            return input.key();
                        }

                        @Override
                        public PortDirection direction() {
                            return PortDirection.INPUT;
                        }

                        @Override
                        public long transfer(long amount, boolean simulate) {
                            return amount;
                        }
                    }).toList();
                }

                @Override
                public List<IMachineRoute> outputRoutes() {
                    return step.pattern().outputs().stream()
                        .map(output -> (IMachineRoute) new IMachineRoute() {
                            @Override
                            public IIngredientKey key() {
                                return output.key();
                            }

                            @Override
                            public PortDirection direction() {
                                return PortDirection.OUTPUT;
                            }

                            @Override
                            public long transfer(long amount, boolean simulate) {
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
                public void release() {}
            });
        }
    }

    private static final class NoOpEvents implements IJobEvents {
    }
}
