package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IStackKey;
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
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftCpuPersistenceTest {
    @Test
    void serviceShouldResumeFromRunningSnapshot() {
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1);
        var plan = new CraftPlan(List.of(step("s1"), step("s2")));
        var codec = new PatternNbtCodec(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);

        var first = new AutocraftJobService(executor());
        first.submitPrepared(List.of(target), plan);
        first.tick();
        var snapshot = first.serializeRunningSnapshot(codec).orElseThrow();

        var restored = new AutocraftJobService(executor());
        restored.restoreRunningSnapshot(snapshot, codec);
        assertEquals(JobState.RUNNING, restored.getJob().orElseThrow().state());
        assertEquals(List.of(target), restored.getJob().orElseThrow().targets());

        while (restored.isBusy()) {
            restored.tick();
        }
        assertTrue(restored.getJob().isEmpty());
    }

    @Test
    void serviceShouldSerializeExecutionSnapshotWithoutSeparatePlanOrRuntimeFields() {
        var plan = new CraftPlan(List.of(step("s1")));
        var service = new AutocraftJobService(executor());
        var codec = new PatternNbtCodec(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);

        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)), plan);
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
                List.of(new CraftAmount(TestStackKey.item("minecraft:cobblestone", ""), 2)),
                List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 2)),
                TestAutocraftHelper.constraints("tinactory:mixer", 0)),
            1,
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)));
        var plan = new CraftPlan(List.of(step));
        var service = new AutocraftJobService(executor());
        var codec = new PatternNbtCodec(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);

        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)), plan);
        service.tick();
        var serialized = service.serializeRunningSnapshot(codec).orElseThrow();
        var restored = new AutocraftJobService(executor());
        restored.restoreRunningSnapshot(serialized, codec);

        var restoredSerialized = restored.serializeRunningSnapshot(codec).orElseThrow();
        var restoredStep = restoredSerialized.getCompound("execution")
            .getCompound("plan")
            .getList("steps", TAG_COMPOUND)
            .getCompound(0);
        assertEquals(1, restoredStep.getList("requiredIntermediateOutputs", TAG_COMPOUND).size());
        assertEquals(1, restoredStep.getList("requiredFinalOutputs", TAG_COMPOUND).size());
    }

    @Test
    void tickShouldReportDirtyStateChangesOnlyWhenJobIsStillBusy() {
        var plan = new CraftPlan(List.of(step("s1")));
        var service = new AutocraftJobService(executor());
        service.submitPrepared(List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)), plan);

        assertTrue(service.tick());
        assertTrue(service.tick());
        assertFalse(service.tick());
        assertFalse(service.tick());
    }

    private static CraftStep step(String id) {
        return new CraftStep(id, new CraftPattern(
            "tinactory:" + id,
            List.of(new CraftAmount(TestStackKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            TestAutocraftHelper.constraints("tinactory:mixer", 0)), 1);
    }

    private static SequentialCraftExecutor executor() {
        return new SequentialCraftExecutor(new NoOpInventory(), new AlwaysMachineAllocator(), IJobEvents.NO_OP);
    }

    private static final class NoOpInventory implements IInventoryView {
        private final Map<IStackKey, Long> amounts = new HashMap<>();

        @Override
        public long amountOf(IStackKey key) {
            return amounts.computeIfAbsent(key, $ -> 64L);
        }

        @Override
        public long extract(IStackKey key, long amount, boolean simulate) {
            var current = amountOf(key);
            var moved = Math.min(current, amount);
            if (!simulate) {
                amounts.put(key, current - moved);
            }
            return moved;
        }

        @Override
        public long insert(IStackKey key, long amount, boolean simulate) {
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
                        public IStackKey key() {
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
                            public IStackKey key() {
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
}
