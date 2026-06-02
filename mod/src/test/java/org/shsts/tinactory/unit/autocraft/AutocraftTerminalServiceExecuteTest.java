package org.shsts.tinactory.unit.autocraft;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobSnapshot;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1))),
            pattern("tinactory:p2", List.of(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 2),
                new CraftAmount(TestStackKey.fluid("minecraft:water", ""), 1000))));
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(patterns),
            new TestCpuRuntime(() -> List.of(), id -> Optional.empty()));

        var requestables = service.listRequestables();

        assertEquals(2, requestables.size());
        assertEquals(PortType.ITEM, requestables.get(0).type());
        assertEquals(PortType.FLUID, requestables.get(1).type());
    }

    @Test
    void executeShouldUseStoredSnapshotAndNotInvokePlannerAgain() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var visibleCpus = new ArrayList<>(List.of(cpu));
        var previewPlanner = new StaticPlanner(planRequiring(
            new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
            new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1)));
        var jobService = new AutocraftJobService(new TestExecutor());
        var service = new AutocraftTerminalService(
            previewPlanner,
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.copyOf(visibleCpus),
                id -> id.equals(cpu) ? Optional.of(jobService) : Optional.empty()));

        service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);
        var execute = service.execute(cpu);

        assertTrue(execute);
        assertTrue(service.previewResult().isEmpty());
        assertEquals(
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1)),
            jobService.getJob().get().targets());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
    }

    @Test
    void executeShouldSubmitPreparedPreviewMemoryUsage() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var jobService = new AutocraftJobService(new TestExecutor(), 64L, 1, 1024L);
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1))),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.of(jobService)),
            50L,
            10L,
            5L);
        var preview = service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);

        assertEquals(60L, preview.memoryUsage());
        assertTrue(service.execute(cpu));
        assertEquals(60L, jobService.getJob().orElseThrow().memoryUsage());
    }

    @Test
    void executeShouldFailWhenPreviewMemoryExceedsCpuLimit() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var jobService = new AutocraftJobService(new TestExecutor(), 64L, 1, 40L);
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1))),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.of(jobService)),
            50L,
            0L,
            0L);
        service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);

        var execute = service.execute(cpu);

        assertFalse(execute);
        assertTrue(jobService.getJob().isEmpty());
        assertTrue(service.previewResult().isSuccess());
    }

    @Test
    void executeShouldFailWhenCpuUnavailable() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var visibleCpus = new ArrayList<>(List.of(cpu));
        var jobService = new AutocraftJobService(new TestExecutor()) {
            @Override
            public boolean isBusy() {
                return true;
            }
        };
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1))),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.copyOf(visibleCpus),
                id -> id.equals(cpu) ? Optional.of(jobService) : Optional.empty()));
        service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);

        var execute = service.execute(cpu);

        assertFalse(execute);
        assertTrue(service.previewResult().isSuccess());
    }

    @Test
    void executeShouldFailWhenCpuOffline() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1))),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.empty()));
        service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);

        var execute = service.execute(cpu);

        assertFalse(execute);
        assertTrue(service.previewResult().isSuccess());
    }

    @Test
    void executeShouldFailWhenPreviewMissing() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var jobService = new AutocraftJobService(new TestExecutor());
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.of(jobService)));

        var execute = service.execute(cpu);

        assertFalse(execute);
        assertTrue(jobService.getJob().isEmpty());
    }

    @Test
    void listCpuStatusesShouldExposeStructuredJobFields() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var targets = List.of(new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 3));
        var job = new AutocraftJobSnapshot(
            targets,
            JobState.BLOCKED,
            1,
            1,
            ExecutionError.FLUSH_BACKPRESSURE,
            256L);
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> id.equals(cpu) ? Optional.of(staticService(job)) : Optional.empty()));

        var statuses = service.listCpuStatuses();

        assertEquals(1, statuses.size());
        assertEquals(cpu, statuses.get(0).cpuId());
        assertEquals(targets, statuses.get(0).targets());
        assertEquals(JobState.BLOCKED, statuses.get(0).state());
        assertEquals(1, statuses.get(0).completedSteps());
        assertEquals(1, statuses.get(0).totalSteps());
        assertEquals(ExecutionError.FLUSH_BACKPRESSURE, statuses.get(0).error());
        assertEquals(1024L, statuses.get(0).memoryLimit());
        assertEquals(256L, statuses.get(0).memoryUsage());
    }

    @Test
    void jobSnapshotShouldPersistAndRestoreMemoryUsage() {
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1);
        var codec = new PatternNbtCodec(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);
        var service = new AutocraftJobService(new TestExecutor(), 64L, 1, 1024L);
        service.submitPrepared(List.of(target), planRequiring(
            new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
            target), 256L);

        var persisted = service.serializeRunningSnapshot(codec).orElseThrow();
        var restored = new AutocraftJobService(new TestExecutor(), 64L, 1, 1024L);
        restored.restoreRunningSnapshot(persisted, codec);

        assertEquals(256L, persisted.getLong("memoryUsage"));
        assertEquals(256L, restored.getJob().orElseThrow().memoryUsage());
    }

    @Test
    void oldJobSnapshotShouldRestoreZeroMemoryUsage() {
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1);
        var codec = new PatternNbtCodec(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);
        var service = new AutocraftJobService(new TestExecutor(), 64L, 1, 1024L);
        service.submitPrepared(List.of(target), planRequiring(
            new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
            target), 256L);
        var persisted = service.serializeRunningSnapshot(codec).orElseThrow();
        persisted.remove("memoryUsage");

        var restored = new AutocraftJobService(new TestExecutor(), 64L, 1, 1024L);
        restored.restoreRunningSnapshot(persisted, codec);

        assertEquals(0L, restored.getJob().orElseThrow().memoryUsage());
    }

    @Test
    void listCpuStatusesShouldExposeOfflineEntryWhenVisibleServiceCannotBeResolved() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.empty()));

        var statuses = service.listCpuStatuses();

        assertEquals(1, statuses.size());
        assertEquals(cpu, statuses.get(0).cpuId());
        assertEquals(JobState.FAILED, statuses.get(0).state());
        assertEquals(List.of(), statuses.get(0).targets());
        assertEquals(0, statuses.get(0).completedSteps());
        assertEquals(0, statuses.get(0).totalSteps());
        assertEquals(ExecutionError.OFFLINE, statuses.get(0).error());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> outputs) {
        return TestAutocraftHelper.pattern(id, List.of(
                new CraftAmount(TestStackKey.item("minecraft:cobblestone", ""), 1)),
            outputs, TestAutocraftHelper.constraints("tinactory:mixer", 0));
    }

    private static IPatternRepository repo(List<CraftPattern> patterns) {
        return new IPatternRepository() {
            @Override
            public List<CraftPattern> findPatternsProducing(IStackKey key) {
                var out = new ArrayList<CraftPattern>();
                for (var pattern : patterns.stream().sorted(Comparator.comparing(CraftPattern::patternUuid)).toList()) {
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
            public List<IStackKey> listRequestables() {
                return patterns.stream()
                    .flatMap(pattern -> pattern.outputs().stream())
                    .map(CraftAmount::key)
                    .distinct()
                    .sorted()
                    .toList();
            }

            @Override
            public List<CraftPattern> listPatterns() {
                return patterns.stream().sorted(Comparator.comparing(CraftPattern::patternUuid)).toList();
            }

            @Override
            public boolean containsPatternUuid(UUID patternUuid) {
                return patterns.stream().anyMatch(pattern -> pattern.patternUuid().equals(patternUuid));
            }

            @Override
            public boolean addPattern(CraftPattern pattern) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removePattern(UUID patternUuid) {
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
        var pattern = TestAutocraftHelper.pattern("tinactory:test",
            List.of(input),
            List.of(output),
            TestAutocraftHelper.constraints("tinactory:mixer", 0));
        return new CraftPlan(List.of(new CraftStep("s1", pattern, 1L)));
    }

    private static IAutocraftService staticService(AutocraftJobSnapshot job) {
        return new IAutocraftService() {
            @Override
            public boolean isBusy() {
                return job.state() == JobState.RUNNING || job.state() == JobState.BLOCKED;
            }

            @Override
            public long memoryLimit() {
                return 1024L;
            }

            @Override
            public Optional<AutocraftJobSnapshot> getJob() {
                return Optional.of(job);
            }

            @Override
            public boolean cancel() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void submitPrepared(List<CraftAmount> targets, CraftPlan plan, long memoryUsage) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private record TestCpuRuntime(
        Supplier<List<UUID>> visibleCpuSupplier,
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
        public Optional<IMachine> findVisibleCpuMachine(UUID cpuId) {
            return Optional.empty();
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
        public PlanResult plan(List<CraftAmount> targets) {
            calls++;
            return PlanResult.completed(plan);
        }

        @Override
        public void start(List<CraftAmount> targets) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<PlanResult> advance(int budget) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestExecutor implements ICraftExecutor {
        private boolean active;

        @Override
        public void start(CraftPlan plan) {
            active = true;
        }

        @Override
        public void restore(CompoundTag tag, PatternNbtCodec codec) {
            active = true;
        }

        @Override
        public void runCycle(long transmissionBandwidth) {}

        @Override
        public void cancel() {}

        @Override
        public boolean isBusy() {
            return active;
        }

        @Override
        public JobState state() {
            return active ? JobState.RUNNING : JobState.IDLE;
        }

        @Override
        public ExecutionError error() {
            return ExecutionError.NONE;
        }

        @Override
        public int completedSteps() {
            return 0;
        }

        @Override
        public int totalSteps() {
            return 0;
        }

        @Override
        public CompoundTag serialize(PatternNbtCodec codec) {
            return new CompoundTag();
        }
    }
}
