package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.PlanningState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestInventoryView;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GoalReductionPlannerErrorTest {
    @Test
    void plannerShouldDetectDirectCycle() {
        var a = TestStackKey.item("tinactory:a", "");
        var loop = pattern("tinactory:a_from_a", List.of(new CraftAmount(a, 1)), List.of(new CraftAmount(a, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(loop)), TestInventoryView.empty());

        var snapshot = planner.plan(List.of(new CraftAmount(a, 1)));

        assertEquals(PlanningState.FAILED, snapshot.state());
        assertEquals(PlanError.Code.CYCLE_DETECTED, snapshot.error().code());
        assertNotNull(snapshot.error().cyclePath());
        assertEquals(a, snapshot.error().cyclePath().get(0));
        assertSummaryEntry(snapshot.summary(), a, 0, 1, 1);
    }

    @Test
    void plannerShouldDetectIndirectCycleWithPath() {
        var a = TestStackKey.item("tinactory:a", "");
        var b = TestStackKey.item("tinactory:b", "");
        var c = TestStackKey.item("tinactory:c", "");

        var aFromB = pattern("tinactory:a_from_b", List.of(new CraftAmount(b, 1)), List.of(new CraftAmount(a, 1)));
        var bFromC = pattern("tinactory:b_from_c", List.of(new CraftAmount(c, 1)), List.of(new CraftAmount(b, 1)));
        var cFromA = pattern("tinactory:c_from_a", List.of(new CraftAmount(a, 1)), List.of(new CraftAmount(c, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(aFromB, bFromC, cFromA)), TestInventoryView.empty());

        var snapshot = planner.plan(List.of(new CraftAmount(a, 1)));

        assertEquals(PlanningState.FAILED, snapshot.state());
        assertEquals(PlanError.Code.CYCLE_DETECTED, snapshot.error().code());
        assertEquals(List.of(a, b, c, a), snapshot.error().cyclePath());
    }

    @Test
    void plannerShouldReportMissingPatternForTarget() {
        var missing = TestStackKey.item("tinactory:unknown", "");
        var planner = new GoalReductionPlanner(repo(List.of()), TestInventoryView.empty());

        var snapshot = planner.plan(List.of(new CraftAmount(missing, 1)));

        assertEquals(PlanningState.FAILED, snapshot.state());
        assertEquals(PlanError.Code.MISSING_PATTERN, snapshot.error().code());
        assertEquals(missing, snapshot.error().targetKey());
        assertSummaryEntry(snapshot.summary(), missing, 0, 1, 0);
    }

    @Test
    void plannerShouldReportUnsatisfiedBaseResource() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var gear = TestStackKey.item("tinactory:gear", "");
        var gearPattern = pattern(
            "tinactory:gear_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(gear, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(gearPattern)), TestInventoryView.empty());

        var snapshot = planner.plan(List.of(new CraftAmount(gear, 1)));

        assertEquals(PlanningState.FAILED, snapshot.state());
        assertEquals(PlanError.Code.UNSATISFIED_BASE_RESOURCE, snapshot.error().code());
        assertEquals(ingot, snapshot.error().targetKey());
        assertEquals(2, snapshot.summary().entries().size());
        assertSummaryEntry(snapshot.summary(), ingot, 0, 2, 0);
        assertSummaryEntry(snapshot.summary(), gear, 0, 0, 1);
    }

    @Test
    void plannerShouldDiscardFailedNestedCandidateSummaryWhenAlternativeSucceeds() {
        var ore = TestStackKey.item("tinactory:ore", "");
        var dust = TestStackKey.item("tinactory:dust", "");
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var gear = TestStackKey.item("tinactory:gear", "");

        var gearFromIngot = pattern(
            "tinactory:gear_from_ingot",
            List.of(new CraftAmount(ingot, 1)),
            List.of(new CraftAmount(gear, 1)));
        var ingotFromOre = pattern(
            "tinactory:a_ingot_from_ore",
            List.of(new CraftAmount(ore, 1)),
            List.of(new CraftAmount(ingot, 1)));
        var ingotFromDust = pattern(
            "tinactory:b_ingot_from_dust",
            List.of(new CraftAmount(dust, 1)),
            List.of(new CraftAmount(ingot, 1)));
        var planner = new GoalReductionPlanner(
            repo(List.of(gearFromIngot, ingotFromOre, ingotFromDust)),
            TestInventoryView.fromAmounts(List.of(new CraftAmount(dust, 1))));

        var snapshot = planner.plan(List.of(new CraftAmount(gear, 2)));

        assertEquals(PlanningState.FAILED, snapshot.state());
        assertEquals(PlanError.Code.UNSATISFIED_BASE_RESOURCE, snapshot.error().code());
        assertEquals(ore, snapshot.error().targetKey());
        assertEquals(4, snapshot.summary().entries().size());
        assertSummaryEntry(snapshot.summary(), dust, 1, 1, 0);
        assertSummaryEntry(snapshot.summary(), ingot, 0, 0, 2);
        assertSummaryEntry(snapshot.summary(), gear, 0, 0, 2);
        assertSummaryEntry(snapshot.summary(), ore, 0, 1, 0);
    }

    @Test
    void plannerShouldSummarizeDesignExampleRepresentativeFailedRoute() {
        var a = TestStackKey.item("tinactory:a", "");
        var b = TestStackKey.item("tinactory:b", "");
        var c = TestStackKey.item("tinactory:c", "");
        var d = TestStackKey.item("tinactory:d", "");
        var e = TestStackKey.item("tinactory:e", "");
        var f = TestStackKey.item("tinactory:f", "");

        var aFromB = pattern("tinactory:a_a_from_b", List.of(new CraftAmount(b, 1)), List.of(new CraftAmount(a, 1)));
        var aFromC = pattern("tinactory:b_a_from_c", List.of(new CraftAmount(c, 1)), List.of(new CraftAmount(a, 1)));
        var dFromE = pattern("tinactory:a_d_from_e", List.of(new CraftAmount(e, 1)), List.of(new CraftAmount(d, 1)));
        var dFromF = pattern("tinactory:b_d_from_f", List.of(new CraftAmount(f, 1)), List.of(new CraftAmount(d, 1)));
        var planner = new GoalReductionPlanner(
            repo(List.of(aFromB, aFromC, dFromE, dFromF)),
            TestInventoryView.fromAmounts(List.of(new CraftAmount(c, 1))));

        var snapshot = planner.plan(List.of(new CraftAmount(a, 1), new CraftAmount(d, 1)));

        assertEquals(PlanningState.FAILED, snapshot.state());
        assertEquals(PlanError.Code.UNSATISFIED_BASE_RESOURCE, snapshot.error().code());
        assertEquals(e, snapshot.error().targetKey());
        assertEquals(4, snapshot.summary().entries().size());
        assertSummaryEntry(snapshot.summary(), c, 1, 1, 0);
        assertSummaryEntry(snapshot.summary(), a, 0, 0, 1);
        assertSummaryEntry(snapshot.summary(), e, 0, 1, 0);
        assertSummaryEntry(snapshot.summary(), d, 0, 0, 1);
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return TestAutocraftHelper.pattern(id, inputs, outputs);
    }

    private static void assertSummaryEntry(
        PlanSummary summary,
        IStackKey key,
        long existingAmount,
        long consumedFromInventory,
        long craftedAmount) {
        assertEquals(
            new PlanSummary.Entry(existingAmount, consumedFromInventory, craftedAmount),
            summary.entries().get(key));
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
}
