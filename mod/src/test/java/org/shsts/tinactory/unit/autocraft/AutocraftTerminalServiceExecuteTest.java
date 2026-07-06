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
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.core.autocraft.pattern.PatternRegistryCache;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobSnapshot;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestStackKey;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestAutocraftHelper.PATTERN_CODECS;

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
            new TestCpuRuntime(List::of, id -> Optional.empty()));

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
            jobService.getJob().orElseThrow().targets());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
    }

    @Test
    void executeShouldSubmitPreparedPlanMemoryUsage() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var jobService = new AutocraftJobService(new TestExecutor(), 64L, 64L, 1, 1024L);
        var input = TestStackKey.item("minecraft:iron_ingot", "");
        var plan = planRequiring(
            new CraftAmount(input, 1),
            new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1),
            new PlanSummary(Map.of(input, new PlanSummary.Entry(8, 1, 1))),
            75L);
        var service = new AutocraftTerminalService(
            new StaticPlanner(PlanResult.completed(plan)),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.of(jobService)));
        var preview = service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);

        assertNotNull(preview.plan());
        assertEquals(75L, preview.plan().memoryUsage());
        assertTrue(service.execute(cpu));
        assertEquals(75L, jobService.getJob().orElseThrow().memoryUsage());
    }

    @Test
    void executeShouldFailWhenPreviewMemoryExceedsCpuLimit() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var jobService = new AutocraftJobService(new TestExecutor(), 64L, 64L, 1, 40L);
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1),
                PlanSummary.empty(),
                50L)),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> Optional.of(jobService)));
        service.preview(TestStackKey.item("minecraft:iron_plate", ""), 1);

        var execute = service.execute(cpu);

        assertFalse(execute);
        assertTrue(jobService.getJob().isEmpty());
        assertNotNull(service.previewResult().orElseThrow().plan());
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
        assertNotNull(service.previewResult().orElseThrow().plan());
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
        assertNotNull(service.previewResult().orElseThrow().plan());
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
            ExecutionError.FLUSH_BLOCKED,
            256L);
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            repo(List.of()),
            new TestCpuRuntime(
                () -> List.of(cpu),
                id -> id.equals(cpu) ? Optional.of(staticService(job)) : Optional.empty()));

        var statuses = service.listCpuStatuses();
        var status = statuses.getFirst();

        assertEquals(1, statuses.size());
        assertEquals(cpu, status.cpuId());
        assertEquals(targets, status.targets());
        assertEquals(JobState.BLOCKED, status.state());
        assertEquals(1, status.completedSteps());
        assertEquals(1, status.totalSteps());
        assertEquals(ExecutionError.FLUSH_BLOCKED, status.error());
        assertEquals(1024L, status.memoryLimit());
        assertEquals(256L, status.memoryUsage());
    }

    @Test
    void jobSnapshotShouldPersistAndRestoreMemoryUsage() {
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1);
        var service = new AutocraftJobService(new TestExecutor(), 64L, 64L, 1, 1024L);
        service.submitPrepared(List.of(target), planRequiring(
            new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
            target,
            PlanSummary.empty(),
            256L));

        var persisted = service.serializeRunningSnapshot(PATTERN_CODECS).orElseThrow();
        var restored = new AutocraftJobService(new TestExecutor(), 64L, 64L, 1, 1024L);
        restored.restoreRunningSnapshot(persisted, PATTERN_CODECS);

        assertEquals(256L, persisted.getLong("memoryUsage"));
        assertEquals(256L, restored.getJob().orElseThrow().memoryUsage());
    }

    @Test
    void oldJobSnapshotShouldRestoreZeroMemoryUsage() {
        var target = new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1);
        var service = new AutocraftJobService(new TestExecutor(), 64L, 64L, 1, 1024L);
        service.submitPrepared(List.of(target), planRequiring(
            new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
            target,
            PlanSummary.empty(),
            256L));
        var persisted = service.serializeRunningSnapshot(PATTERN_CODECS).orElseThrow();
        persisted.remove("memoryUsage");

        var restored = new AutocraftJobService(new TestExecutor(), 64L, 64L, 1, 1024L);
        restored.restoreRunningSnapshot(persisted, PATTERN_CODECS);

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
        var status = statuses.getFirst();

        assertEquals(1, statuses.size());
        assertEquals(cpu, status.cpuId());
        assertEquals(JobState.FAILED, status.state());
        assertEquals(List.of(), status.targets());
        assertEquals(0, status.completedSteps());
        assertEquals(0, status.totalSteps());
        assertEquals(ExecutionError.OFFLINE, status.error());
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
                    .sorted(PatternRegistryCache.KEY_DISPLAY_ORDER)
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
        return planRequiring(input, output, PlanSummary.empty(), 0L);
    }

    private static CraftPlan planRequiring(
        CraftAmount input,
        CraftAmount output,
        PlanSummary summary,
        long memoryUsage) {

        var pattern = TestAutocraftHelper.pattern("tinactory:test",
            List.of(input),
            List.of(output),
            TestAutocraftHelper.constraints("tinactory:mixer", 0));
        return new CraftPlan(List.of(new CraftStep("s1", pattern, 1L)), summary, memoryUsage);
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
            public void submitPrepared(List<CraftAmount> targets, CraftPlan plan) {
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
        private final PlanResult result;
        private int calls;

        private StaticPlanner() {
            this(new CraftPlan(List.of()));
        }

        private StaticPlanner(CraftPlan plan) {
            this.result = PlanResult.completed(plan);
        }

        private StaticPlanner(PlanResult result) {
            this.result = result;
        }

        @Override
        public PlanResult plan(List<CraftAmount> targets) {
            calls++;
            return result;
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
        public void restore(CompoundTag tag, PatternCodec codec) {
            active = true;
        }

        @Override
        public void runCycle(long itemBandwidth, long fluidBandwidth) {}

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
        public CompoundTag serialize(PatternCodec codec) {
            return new CompoundTag();
        }
    }
}
