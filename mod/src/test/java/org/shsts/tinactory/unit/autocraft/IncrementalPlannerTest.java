package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import org.shsts.tinactory.unit.fixture.TestInventoryView;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.PlanningState;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlannerSnapshot;
import org.shsts.tinactory.core.autocraft.plan.PlannerSession;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IncrementalPlannerTest {
    @Test
    void incrementalPlannerShouldReturnRunningWhenBudgetIsZero() {
        var planner = planner(repo(List.of()), List.of());
        var key = TestIngredientKey.item("tinactory:gear", "");
        var session = planner.startSession(List.of(new CraftAmount(key, 1)));

        var progress = planner.resume(session, 0);

        assertEquals(PlanningState.RUNNING, progress.state());
        assertNull(progress.plan());
        assertNull(progress.error());
    }

    @Test
    void incrementalPlannerShouldMakePartialProgressAndThenFinish() {
        var ingot = TestIngredientKey.item("tinactory:ingot", "");
        var plate = TestIngredientKey.item("tinactory:plate", "");
        var gear = TestIngredientKey.item("tinactory:gear", "");
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
        var session = planner.startSession(List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1)));

        var first = planner.resume(session, 1);
        var progress = runUntilTerminal(planner, session, 64);

        assertEquals(PlanningState.RUNNING, first.state());
        assertEquals(PlanningState.COMPLETED, progress.state());
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
        assertEquals(
            planner.plan(List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1))).plan(),
            progress.plan());
    }

    @Test
    void incrementalPlannerShouldMatchSynchronousFailure() {
        var gear = TestIngredientKey.item("tinactory:gear", "");
        var planner = planner(repo(List.of()), List.of());
        var session = planner.startSession(List.of(new CraftAmount(gear, 1)));

        var progress = planner.resume(session, 1);
        var sync = planner.plan(List.of(new CraftAmount(gear, 1)));

        assertEquals(PlanningState.FAILED, progress.state());
        assertEquals(sync.error(), progress.error());
    }

    @Test
    void incrementalPlannerShouldBeDeterministicAcrossReplays() {
        var ingot = TestIngredientKey.item("tinactory:ingot", "");
        var plate = TestIngredientKey.item("tinactory:plate", "");
        var gear = TestIngredientKey.item("tinactory:gear", "");
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
        var planner = planner(repo(List.of(platePattern, gearPattern)), available);

        var firstSession = planner.startSession(targets);
        var secondSession = planner.startSession(targets);
        var firstRunA = planner.resume(firstSession, 1);
        var firstRunB = planner.resume(firstSession, 1);
        var secondRunA = planner.resume(secondSession, 1);
        var secondRunB = planner.resume(secondSession, 1);

        assertEquals(firstRunA.state(), secondRunA.state());
        assertEquals(firstRunB.state(), secondRunB.state());
        assertEquals(firstRunB, secondRunB);
    }

    @Test
    void incrementalPlannerBudgetShouldAdvanceWithinSingleRootTargetExpansion() {
        var ingot = TestIngredientKey.item("tinactory:ingot", "");
        var plate = TestIngredientKey.item("tinactory:plate", "");
        var gear = TestIngredientKey.item("tinactory:gear", "");
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
        var session = planner.startSession(List.of(new CraftAmount(gear, 1)));

        var first = planner.resume(session, 1);
        var progress = runUntilTerminal(planner, session, 64);

        assertEquals(PlanningState.RUNNING, first.state());
        assertEquals(PlanningState.COMPLETED, progress.state());
        assertNotNull(progress.plan());
        assertEquals(
            planner.plan(List.of(new CraftAmount(gear, 1))),
            progress);
    }

    private static GoalReductionPlanner planner(IPatternRepository repo, List<CraftAmount> available) {
        return new GoalReductionPlanner(repo, TestInventoryView.fromAmounts(available));
    }

    private static PlannerSnapshot runUntilTerminal(
        GoalReductionPlanner planner,
        PlannerSession session,
        int maxSteps) {
        var progress = new PlannerSnapshot(PlanningState.RUNNING, null, null);
        for (var i = 0; i < maxSteps && progress.state() == PlanningState.RUNNING; i++) {
            progress = planner.resume(session, 1);
        }
        return progress;
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs,
            new MachineRequirement(new ResourceLocation("tinactory", "machine"), 1, List.of()));
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
}
