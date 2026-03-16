package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.service.AutocraftExecuteResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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
            List::of,
            List::of,
            List::of);

        var requestables = service.listRequestables();

        assertEquals(2, requestables.size());
        assertEquals(PortType.ITEM, requestables.get(0).type());
        assertEquals(PortType.FLUID, requestables.get(1).type());
    }

    @Test
    void executeShouldUseStoredSnapshotAndNotInvokePlannerAgain() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        var previewPlanner = new StaticPlanner(planRequiring(
            new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1),
            new CraftAmount(TestIngredientKey.item("minecraft:iron_plate", ""), 1)));
        var jobService = new AutocraftJobService(cpu,
            (targets, available) -> {
                throw new IllegalStateException("planner should not be called during execute tick");
            },
            TestExecutor::new,
            List::of);
        var service = new AutocraftTerminalService(
            previewPlanner,
            repo(List.of()),
            () -> List.copyOf(availableCpus),
            () -> List.copyOf(availableCpus),
            () -> List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 64)),
            resolverFor(cpu, jobService));

        service.preview(TestIngredientKey.item("minecraft:iron_plate", ""), 1);
        var execute = service.execute(cpu);

        assertTrue(execute.isSuccess());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
    }

    @Test
    void executeShouldFailWhenCpuUnavailable() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        // TODO
        var jobService = new AutocraftJobService(cpu,
            (targets, available) -> PlanResult.success(new CraftPlan(List.of())),
            TestExecutor::new, List::of) {
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
            () -> List.copyOf(availableCpus),
            () -> List.copyOf(availableCpus),
            () -> List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 64)),
            resolverFor(cpu, jobService));
        service.preview(TestIngredientKey.item("minecraft:iron_plate", ""), 1);
        availableCpus.clear();

        var execute = service.execute(cpu);

        assertFalse(execute.isSuccess());
        assertEquals(AutocraftExecuteResult.Code.CPU_BUSY, execute.errorCode());
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

    private static Function<UUID, IAutocraftService> resolverFor(
        UUID cpu,
        IAutocraftService service) {
        return id -> id.equals(cpu) ? service : null;
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
        public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
            calls++;
            return PlanResult.success(plan);
        }
    }

    private static final class TestExecutor implements ICraftExecutor {
        @Override
        public void start(CraftPlan plan) {}

        @Override
        public void runCycle(long transmissionBandwidth) {}

        @Override
        public void cancel() {}

        @Override
        public ExecutionState state() {
            return ExecutionState.RUNNING;
        }

        @Override
        public ExecutionError error() {
            return null;
        }

        @Override
        public ExecutionDetails details() {
            return new ExecutionDetails(
                ExecutionDetails.Phase.RUN_STEP, null, null, 0, Map.of(), Map.of(), Map.of(), null);
        }
    }
}
