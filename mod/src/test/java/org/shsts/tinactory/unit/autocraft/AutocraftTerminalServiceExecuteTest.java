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
import org.shsts.tinactory.core.autocraft.service.AutocraftExecuteResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobSnapshot;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
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

        assertTrue(execute.isSuccess());
        assertTrue(service.previewResult().isEmpty());
        assertEquals(
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1)),
            jobService.getJob().get().targets());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
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

        assertFalse(execute.isSuccess());
        assertEquals(AutocraftExecuteResult.Code.CPU_BUSY, execute.errorCode());
        assertTrue(service.previewResult().isSuccess());
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
            ExecutionError.FLUSH_BACKPRESSURE);
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
                return patterns.stream().sorted(Comparator.comparing(CraftPattern::patternId)).toList();
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
        @Override
        public void start(CraftPlan plan) {}

        @Override
        public void restore(CompoundTag tag, PatternNbtCodec codec) {}

        @Override
        public void runCycle(long transmissionBandwidth) {}

        @Override
        public void cancel() {}

        @Override
        public boolean isBusy() {
            return true;
        }

        @Override
        public JobState state() {
            return JobState.RUNNING;
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
