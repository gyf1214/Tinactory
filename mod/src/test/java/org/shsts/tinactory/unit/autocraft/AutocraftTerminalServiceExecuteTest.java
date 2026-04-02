package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutorSnapshot;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlannerSnapshot;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.service.AutocraftExecuteResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobSnapshot;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServiceExecuteTest {
    @Test
    void listRequestablesShouldReturnDedupedOutputsFromStoredPatterns() {
        var patterns = List.of(
            pattern("tinactory:p1", List.of(
                new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1))),
            pattern("tinactory:p2", List.of(
                new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 2),
                new CraftAmount(TestIngredientKey.fluid("minecraft:water", ""), 1000))));
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(patterns),
            new TestCpuRuntime(() -> List.of(), () -> List.of(), id -> Optional.empty()));

        var requestables = service.listRequestables();

        assertEquals(2, requestables.size());
        assertEquals(PortType.ITEM, requestables.get(0).type());
        assertEquals(PortType.FLUID, requestables.get(1).type());
    }

    @Test
    void executeShouldUseStoredSnapshotAndNotInvokePlannerAgain() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        var visibleCpus = new ArrayList<>(List.of(cpu));
        var previewPlanner = new StaticPlanner(planRequiring(
            new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1),
            new CraftAmount(TestIngredientKey.item("minecraft:iron_plate", ""), 1)));
        var jobService = new AutocraftJobService(new TestExecutor());
        var service = new AutocraftTerminalService(
            previewPlanner,
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.copyOf(visibleCpus),
                () -> List.copyOf(availableCpus),
                id -> id.equals(cpu) ? Optional.of(jobService) : Optional.empty()));

        service.preview(TestIngredientKey.item("minecraft:iron_plate", ""), 1);
        var execute = service.execute(cpu);

        assertTrue(execute.isSuccess());
        assertTrue(service.previewResult().isEmpty());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
    }

    @Test
    void executeShouldFailWhenCpuUnavailable() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        var visibleCpus = new ArrayList<>(List.of(cpu));
        var jobService = new AutocraftJobService(new TestExecutor()) {
            @Override
            public boolean isBusy() {
                return true;
            }
        };
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestIngredientKey.item("minecraft:iron_plate", ""), 1))),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.copyOf(visibleCpus),
                () -> List.copyOf(availableCpus),
                id -> id.equals(cpu) ? Optional.of(jobService) : Optional.empty()));
        service.preview(TestIngredientKey.item("minecraft:iron_plate", ""), 1);
        availableCpus.clear();

        var execute = service.execute(cpu);

        assertFalse(execute.isSuccess());
        assertEquals(AutocraftExecuteResult.Code.CPU_BUSY, execute.errorCode());
        assertTrue(service.previewResult().isSuccess());
    }

    @Test
    void listCpuStatusesShouldExposeStructuredJobFields() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var targets = List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_plate", ""), 3));
        var execution = new ExecutorSnapshot(
            JobState.BLOCKED,
            ExecutionPhase.FLUSHING,
            ExecutionError.FLUSH_BACKPRESSURE,
            null,
            new CraftPlan(List.of(new CraftStep(
                "s1",
                pattern("tinactory:test", List.of(
                    new CraftAmount(TestIngredientKey.item("minecraft:iron_plate", ""), 3))),
                1L))),
            1,
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            null);
        var job = new AutocraftJobSnapshot(UUID.fromString("22222222-2222-2222-2222-222222222222"), targets, execution);
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                () -> List.of(),
                id -> id.equals(cpu) ? Optional.of(staticService(job)) : Optional.empty()));

        var statuses = service.listCpuStatuses();

        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0) instanceof CpuStatusEntry);
        assertEquals(cpu, statuses.get(0).cpuId());
        assertFalse(statuses.get(0).available());
        assertEquals(targets, statuses.get(0).targets());
        assertEquals(JobState.BLOCKED, statuses.get(0).state());
        assertEquals(ExecutionPhase.FLUSHING, statuses.get(0).phase());
        assertEquals(1, statuses.get(0).nextStepIndex());
        assertEquals(1, statuses.get(0).stepCount());
        assertEquals(ExecutionError.FLUSH_BACKPRESSURE, statuses.get(0).error());
        assertTrue(statuses.get(0).cancellable());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> outputs) {
        return new CraftPattern(id, List.of(
            new CraftAmount(TestIngredientKey.item("minecraft:cobblestone", ""), 1)),
            outputs, new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }

    private static IPatternRepository repo(List<CraftPattern> patterns) {
        return new IPatternRepository() {
            @Override
            public List<CraftPattern> findPatternsProducing(IIngredientKey key) {
                var out = new ArrayList<CraftPattern>();
                for (var pattern : patterns.stream().sorted(Comparator.comparing(CraftPattern::patternId)).toList()) {
                    for (var output : pattern.outputs()) {
                        if (output.key().equals(key)) {
                            out.add(pattern);
                            break;
                        }
                    }
                }
                return out;
            }

            @Override
            public List<IIngredientKey> listRequestables() {
                return patterns.stream()
                    .flatMap(pattern -> pattern.outputs().stream())
                    .map(CraftAmount::key)
                    .distinct()
                    .sorted()
                    .toList();
            }

            @Override
            public boolean containsPatternId(String patternId) {
                return patterns.stream().anyMatch(pattern -> pattern.patternId().equals(patternId));
            }

            @Override
            public boolean addPattern(CraftPattern pattern) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removePattern(String patternId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean updatePattern(CraftPattern pattern) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addCellPort(UUID machineId, int priority, int slotIndex, IPatternCellPort port) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int removeCellPorts(UUID machineId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {}
        };
    }

    private static CraftPlan planRequiring(CraftAmount input, CraftAmount output) {
        var pattern = new CraftPattern("tinactory:test",
            List.of(input),
            List.of(output),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
        return new CraftPlan(List.of(new CraftStep("s1", pattern, 1L)));
    }

    private static IAutocraftService staticService(AutocraftJobSnapshot job) {
        return new IAutocraftService() {
            @Override
            public boolean isBusy() {
                return job.execution().state() == JobState.RUNNING || job.execution().state() == JobState.BLOCKED;
            }

            @Override
            public Optional<AutocraftJobSnapshot> getJob() {
                return Optional.of(job);
            }

            @Override
            public boolean cancel(UUID id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UUID submitPrepared(List<CraftAmount> targets, CraftPlan plan) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private record TestCpuRuntime(
        Supplier<List<UUID>> visibleCpuSupplier,
        Supplier<List<UUID>> availableCpuSupplier,
        Function<UUID, Optional<IAutocraftService>> serviceResolver) implements ICpuRuntime {
        @Override
        public void registerCpu(IMachine machine, IAutocraftService service) {}

        @Override
        public void unregisterCpu(UUID cpuId) {}

        @Override
        public List<UUID> listVisibleCpus() {
            return visibleCpuSupplier.get();
        }

        @Override
        public List<UUID> listAvailableCpus() {
            return availableCpuSupplier.get();
        }

        @Override
        public Optional<IAutocraftService> findVisibleService(UUID cpuId) {
            return serviceResolver.apply(cpuId);
        }
    }

    private static final class StaticPlanner implements ICraftPlanner {
        private final CraftPlan plan;
        private int calls;

        private StaticPlanner() {
            this(new CraftPlan(List.of()));
        }

        private StaticPlanner(CraftPlan plan) {
            this.plan = plan;
        }

        @Override
        public PlannerSnapshot plan(List<CraftAmount> targets) {
            calls++;
            return PlannerSnapshot.completed(plan);
        }
    }

    private static final class TestExecutor implements ICraftExecutor {
        @Override
        public void start(CraftPlan plan) {}

        @Override
        public void restore(ExecutorSnapshot snapshot) {}

        @Override
        public void runCycle(long transmissionBandwidth) {}

        @Override
        public void cancel() {}

        @Override
        public ExecutorSnapshot snapshot() {
            return new ExecutorSnapshot(
                JobState.RUNNING,
                ExecutionPhase.RUN_STEP,
                ExecutionError.NONE,
                null,
                new CraftPlan(List.of()),
                0,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                null);
        }
    }
}
