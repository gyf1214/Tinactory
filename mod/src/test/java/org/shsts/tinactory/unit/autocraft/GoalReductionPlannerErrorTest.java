package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GoalReductionPlannerErrorTest {
    @Test
    void plannerShouldDetectDirectCycle() {
        var a = CraftKey.item("tinactory:a", "");
        var loop = pattern("tinactory:a_from_a", List.of(new CraftAmount(a, 1)), List.of(new CraftAmount(a, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(loop)));

        var result = planner.plan(List.of(new CraftAmount(a, 1)), List.of());

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.CYCLE_DETECTED, result.error().code());
        assertNotNull(result.error().cyclePath());
        assertEquals(a, result.error().cyclePath().get(0));
    }

    @Test
    void plannerShouldDetectIndirectCycleWithPath() {
        var a = CraftKey.item("tinactory:a", "");
        var b = CraftKey.item("tinactory:b", "");
        var c = CraftKey.item("tinactory:c", "");

        var aFromB = pattern("tinactory:a_from_b", List.of(new CraftAmount(b, 1)), List.of(new CraftAmount(a, 1)));
        var bFromC = pattern("tinactory:b_from_c", List.of(new CraftAmount(c, 1)), List.of(new CraftAmount(b, 1)));
        var cFromA = pattern("tinactory:c_from_a", List.of(new CraftAmount(a, 1)), List.of(new CraftAmount(c, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(aFromB, bFromC, cFromA)));

        var result = planner.plan(List.of(new CraftAmount(a, 1)), List.of());

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.CYCLE_DETECTED, result.error().code());
        assertEquals(List.of(a, b, c, a), result.error().cyclePath());
    }

    @Test
    void plannerShouldReportMissingPatternForTarget() {
        var missing = CraftKey.item("tinactory:unknown", "");
        var planner = new GoalReductionPlanner(repo(List.of()));

        var result = planner.plan(List.of(new CraftAmount(missing, 1)), List.of());

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.MISSING_PATTERN, result.error().code());
        assertEquals(missing, result.error().targetKey());
    }

    @Test
    void plannerShouldReportUnsatisfiedBaseResource() {
        var ingot = CraftKey.item("tinactory:ingot", "");
        var gear = CraftKey.item("tinactory:gear", "");
        var gearPattern = pattern(
            "tinactory:gear_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(gear, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(gearPattern)));

        var result = planner.plan(List.of(new CraftAmount(gear, 1)), List.of());

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.UNSATISFIED_BASE_RESOURCE, result.error().code());
        assertEquals(ingot, result.error().targetKey());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return new CraftPattern(id, inputs, outputs, new MachineRequirement("tinactory:machine", 1, List.of()));
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
