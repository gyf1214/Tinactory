package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestInventoryView;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalPlannerTest {
    @Test
    void incrementalPlannerShouldReturnRunningWhenBudgetIsZero() {
        var planner = planner(repo(List.of()), List.of());
        var key = TestStackKey.item("tinactory:gear", "");
        planner.start(List.of(new CraftAmount(key, 1)));

        var progress = planner.advance(0);

        assertTrue(progress.isEmpty());
    }

    @Test
    void incrementalPlannerShouldMakePartialProgressAndThenFinish() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var gear = TestStackKey.item("tinactory:gear", "");
        var platePattern = pattern(
            "tinactory:plate_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(plate, 1)));
        var gearPattern = pattern(
            "tinactory:gear_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(gear, 1)));
        var available = List.of(new CraftAmount(ingot, 4));
        var planner = planner(repo(List.of(platePattern, gearPattern)), available);
        planner.start(List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1)));

        var first = planner.advance(1);
        var progress = runUntilTerminal(planner, 64);

        assertTrue(first.isEmpty());
        assertNotNull(progress.plan());
        var plannedPlateIntermediate = progress.plan().steps().stream()
            .filter(step -> step.pattern().patternId().equals("tinactory:plate_from_ingot"))
            .flatMap(step -> step.requiredIntermediateOutputs().stream())
            .filter(amount -> amount.key().equals(plate))
            .mapToLong(CraftAmount::amount)
            .sum();
        var plannedPlateFinal = progress.plan().steps().stream()
            .filter(step -> step.pattern().patternId().equals("tinactory:plate_from_ingot"))
            .flatMap(step -> step.requiredFinalOutputs().stream())
            .filter(amount -> amount.key().equals(plate))
            .mapToLong(CraftAmount::amount)
            .sum();
        assertEquals(1L, plannedPlateIntermediate);
        assertEquals(1L, plannedPlateFinal);
        var sync = planner.plan(List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1)));
        assertEquals(sync.plan(), progress.plan());
        assertEquals(sync.summary(), progress.summary());
    }

    @Test
    void incrementalPlannerShouldMatchSynchronousFailure() {
        var gear = TestStackKey.item("tinactory:gear", "");
        var planner = planner(repo(List.of()), List.of());
        planner.start(List.of(new CraftAmount(gear, 1)));

        var progress = planner.advance(1).orElseThrow();
        var sync = planner.plan(List.of(new CraftAmount(gear, 1)));

        assertEquals(sync.error(), progress.error());
        assertEquals(sync.summary(), progress.summary());
    }

    @Test
    void incrementalPlannerShouldBeDeterministicAcrossReplays() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var gear = TestStackKey.item("tinactory:gear", "");
        var platePattern = pattern(
            "tinactory:plate_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(plate, 1)));
        var gearPattern = pattern(
            "tinactory:gear_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(gear, 1)));
        var targets = List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1));
        var available = List.of(new CraftAmount(ingot, 4));
        var firstPlanner = planner(repo(List.of(platePattern, gearPattern)), available);
        var secondPlanner = planner(repo(List.of(platePattern, gearPattern)), available);

        firstPlanner.start(targets);
        secondPlanner.start(targets);
        var firstRunA = firstPlanner.advance(1);
        var firstRunB = firstPlanner.advance(1);
        var secondRunA = secondPlanner.advance(1);
        var secondRunB = secondPlanner.advance(1);

        assertEquals(firstRunA.isPresent(), secondRunA.isPresent());
        assertEquals(firstRunB.isPresent(), secondRunB.isPresent());
        assertEquals(firstRunB, secondRunB);
    }

    @Test
    void incrementalPlannerBudgetShouldAdvanceWithinSingleRootTargetExpansion() {
        var ingot = TestStackKey.item("tinactory:ingot", "");
        var plate = TestStackKey.item("tinactory:plate", "");
        var gear = TestStackKey.item("tinactory:gear", "");
        var platePattern = pattern(
            "tinactory:plate_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(plate, 1)));
        var gearPattern = pattern(
            "tinactory:gear_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(gear, 1)));
        var available = List.of(new CraftAmount(ingot, 2));
        var planner = planner(repo(List.of(platePattern, gearPattern)), available);
        planner.start(List.of(new CraftAmount(gear, 1)));

        var first = planner.advance(1);
        var progress = runUntilTerminal(planner, 64);

        assertTrue(first.isEmpty());
        assertNotNull(progress.plan());
        assertEquals(
            planner.plan(List.of(new CraftAmount(gear, 1))),
            progress);
    }

    private static GoalReductionPlanner planner(IPatternRepository repo, List<CraftAmount> available) {
        return new GoalReductionPlanner(repo, TestInventoryView.fromAmounts(available));
    }

    private static PlanResult runUntilTerminal(GoalReductionPlanner planner, int maxSteps) {
        for (var i = 0; i < maxSteps; i++) {
            var progress = planner.advance(1);
            if (progress.isPresent()) {
                return progress.get();
            }
        }
        throw new AssertionError("Planner did not finish within budget");
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return TestAutocraftHelper.pattern(id, inputs, outputs);
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
