package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PatternRegistryCache;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.autocraft.service.AutocraftPreview;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServicePreviewTest {

    @Test
    void previewShouldReturnPlan() {
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            new PatternRegistryCache(),
            new TestCpuRuntime());

        var result = service.preview(TestStackKey.item("minecraft:iron_ingot", ""), 3);

        assertTrue(result.isSuccess());
        assertEquals(0, result.planSnapshot().steps().size());
        assertNull(result.error());
        assertEquals(StaticPlanner.SUMMARY, result.summary());
        assertEquals(0L, result.memoryUsage());
        assertEquals(StaticPlanner.SUMMARY, service.preview().get().summary());
    }

    @Test
    void previewShouldComputeMemoryFromStepsAndSummaryEntries() {
        var service = new AutocraftTerminalService(
            new StaticPlanner(PlanResult.completed(twoStepPlan(), StaticPlanner.WIDE_SUMMARY)),
            new PatternRegistryCache(),
            new TestCpuRuntime(),
            10L,
            5L,
            7L);

        var result = service.preview(TestStackKey.item("minecraft:iron_ingot", ""), 3);

        assertEquals(41L, result.memoryUsage());
        assertEquals(41L, service.preview().orElseThrow().memoryUsage());
    }

    @Test
    void previewShouldClampOverflowingMemoryToLongMax() {
        var service = new AutocraftTerminalService(
            new StaticPlanner(PlanResult.completed(twoStepPlan(), StaticPlanner.WIDE_SUMMARY)),
            new PatternRegistryCache(),
            new TestCpuRuntime(),
            Long.MAX_VALUE - 1L,
            10L,
            10L);

        var result = service.preview(TestStackKey.item("minecraft:iron_ingot", ""), 3);

        assertEquals(Long.MAX_VALUE, result.memoryUsage());
    }

    @Test
    void emptyPreviewShouldHaveNoPlanOrErrorAndAnEmptySummary() {
        var preview = AutocraftPreview.empty();

        assertTrue(preview.isEmpty());
        assertNull(preview.planSnapshot());
        assertNull(preview.error());
        assertEquals(PlanSummary.empty(), preview.summary());
        assertEquals(0L, preview.memoryUsage());
    }

    @Test
    void previewShouldReturnStructuredPlanError() {
        var missing = TestStackKey.item("minecraft:iron_ingot", "");
        var summary = new PlanSummary(Map.of(missing, new PlanSummary.Entry(0, 3, 0)));
        var service = new AutocraftTerminalService(
            new StaticPlanner(PlanResult.failed(PlanError.missingPattern(missing), summary)),
            new PatternRegistryCache(),
            new TestCpuRuntime());

        var result = service.preview(missing, 3);

        assertTrue(!result.isSuccess());
        assertEquals(PlanError.Code.MISSING_PATTERN, result.error().code());
        assertEquals(missing, result.error().targetKey());
        assertEquals(summary, result.summary());
        assertEquals(0L, result.memoryUsage());
    }

    private static CraftPlan twoStepPlan() {
        var pattern = TestAutocraftHelper.pattern("tinactory:test",
            List.of(new CraftAmount(TestStackKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            TestAutocraftHelper.constraints("tinactory:mixer", 0));
        return new CraftPlan(List.of(
            new CraftStep("s1", pattern, 1L),
            new CraftStep("s2", pattern, 1L)));
    }

    private static final class StaticPlanner implements ICraftPlanner {
        private static final PlanSummary SUMMARY = new PlanSummary(Map.of(
            TestStackKey.item("minecraft:iron_ingot", ""),
            new PlanSummary.Entry(4, 3, 0)));
        private static final PlanSummary WIDE_SUMMARY = new PlanSummary(Map.of(
            TestStackKey.item("minecraft:iron_ingot", ""),
            new PlanSummary.Entry(4, 3, 0),
            TestStackKey.item("minecraft:iron_plate", ""),
            new PlanSummary.Entry(0, 0, 3),
            TestStackKey.fluid("minecraft:water", ""),
            new PlanSummary.Entry(1000, 1000, 0)));
        private final PlanResult result;

        private StaticPlanner() {
            this(PlanResult.completed(new CraftPlan(List.of()), SUMMARY));
        }

        private StaticPlanner(PlanResult result) {
            this.result = result;
        }

        @Override
        public PlanResult plan(List<CraftAmount> targets) {
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

    private static final class TestCpuRuntime implements ICpuRuntime {
        @Override
        public void registerCpu(IMachine machine, IAutocraftService service) {}

        @Override
        public void unregisterCpu(UUID cpuId) {}

        @Override
        public List<UUID> listVisibleCpus() {
            return List.of();
        }

        @Override
        public Optional<IMachine> findVisibleCpuMachine(UUID cpuId) {
            return Optional.empty();
        }

        @Override
        public Optional<IAutocraftService> findVisibleService(UUID cpuId) {
            return Optional.empty();
        }
    }
}
