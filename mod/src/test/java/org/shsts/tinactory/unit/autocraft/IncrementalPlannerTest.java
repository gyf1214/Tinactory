package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlannerProgress;
import org.shsts.tinactory.core.autocraft.plan.PlannerSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalPlannerTest {
    @Test
    void incrementalPlannerShouldReturnRunningWhenBudgetIsZero() {
        var planner = planner(repo(List.of()));
        var key = CraftKey.item("tinactory:gear", "");
        var session = planner.startSession(List.of(new CraftAmount(key, 1)), List.of());

        var progress = planner.resume(session, 0);

        assertEquals(PlannerProgress.State.RUNNING, progress.state());
        assertNull(progress.result());
    }

    @Test
    void incrementalPlannerShouldMakePartialProgressAndThenFinish() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var gear = CraftKey.item("tinactory:gear", "");
        var platePattern = pattern(
            "tinactory:plate_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(plate, 1)));
        var gearPattern = pattern(
            "tinactory:gear_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(gear, 1)));
        var planner = planner(repo(List.of(platePattern, gearPattern)));
        var session = planner.startSession(
            List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1)),
            List.of(new CraftAmount(ingot, 4)));

        var first = planner.resume(session, 1);
        var progress = runUntilTerminal(planner, session, 64);

        assertEquals(PlannerProgress.State.RUNNING, first.state());
        assertEquals(PlannerProgress.State.DONE, progress.state());
        assertNotNull(progress.result());
        assertTrue(progress.result().isSuccess());
        assertEquals(
            planner.plan(
                List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1)),
                List.of(new CraftAmount(ingot, 4))).plan(),
            progress.result().plan());
    }

    @Test
    void incrementalPlannerShouldMatchSynchronousFailure() {
        var gear = CraftKey.item("tinactory:gear", "");
        var planner = planner(repo(List.of()));
        var session = planner.startSession(List.of(new CraftAmount(gear, 1)), List.of());

        var progress = planner.resume(session, 1);
        var sync = planner.plan(List.of(new CraftAmount(gear, 1)), List.of());

        assertEquals(PlannerProgress.State.FAILED, progress.state());
        assertFalse(progress.result().isSuccess());
        assertEquals(sync.error(), progress.result().error());
    }

    @Test
    void incrementalPlannerShouldBeDeterministicAcrossReplays() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var gear = CraftKey.item("tinactory:gear", "");
        var platePattern = pattern(
            "tinactory:plate_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(plate, 1)));
        var gearPattern = pattern(
            "tinactory:gear_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(gear, 1)));
        var planner = planner(repo(List.of(platePattern, gearPattern)));
        var targets = List.of(new CraftAmount(plate, 1), new CraftAmount(gear, 1));
        var available = List.of(new CraftAmount(ingot, 4));

        var firstSession = planner.startSession(targets, available);
        var secondSession = planner.startSession(targets, available);
        var firstRunA = planner.resume(firstSession, 1);
        var firstRunB = planner.resume(firstSession, 1);
        var secondRunA = planner.resume(secondSession, 1);
        var secondRunB = planner.resume(secondSession, 1);

        assertEquals(firstRunA.state(), secondRunA.state());
        assertEquals(firstRunB.state(), secondRunB.state());
        assertEquals(firstRunB.result(), secondRunB.result());
    }

    @Test
    void incrementalPlannerBudgetShouldAdvanceWithinSingleRootTargetExpansion() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var gear = CraftKey.item("tinactory:gear", "");
        var platePattern = pattern(
            "tinactory:plate_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(plate, 1)));
        var gearPattern = pattern(
            "tinactory:gear_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(gear, 1)));
        var planner = planner(repo(List.of(platePattern, gearPattern)));
        var session = planner.startSession(List.of(new CraftAmount(gear, 1)), List.of(new CraftAmount(ingot, 2)));

        var first = planner.resume(session, 1);
        var progress = runUntilTerminal(planner, session, 64);

        assertEquals(PlannerProgress.State.RUNNING, first.state());
        assertEquals(PlannerProgress.State.DONE, progress.state());
        assertNotNull(progress.result());
        assertEquals(
            planner.plan(List.of(new CraftAmount(gear, 1)), List.of(new CraftAmount(ingot, 2))),
            progress.result());
    }

    private static GoalReductionPlanner planner(IPatternRepository repo) {
        return new GoalReductionPlanner(repo);
    }

    private static PlannerProgress runUntilTerminal(
        GoalReductionPlanner planner,
        PlannerSession session,
        int maxSteps) {
        var progress = PlannerProgress.running();
        for (var i = 0; i < maxSteps && progress.state() == PlannerProgress.State.RUNNING; i++) {
            progress = planner.resume(session, 1);
        }
        return progress;
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs,
            new MachineRequirement(new ResourceLocation("tinactory", "machine"), 1, List.of()));
    }

    private static IPatternRepository repo(List<CraftPattern> patterns) {
        return key -> {
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
        };
    }
}
