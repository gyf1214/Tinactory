package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalReductionPlannerTest {
    @Test
    void plannerShouldBuildSimpleChainInDependencyOrder() {
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
        var planner = new GoalReductionPlanner(repo(List.of(platePattern, gearPattern)));

        var result = planner.plan(List.of(new CraftAmount(gear, 1)), List.of(new CraftAmount(ingot, 2)));

        assertTrue(result.isSuccess());
        var steps = result.plan().steps();
        assertEquals(2, steps.size());
        assertEquals("tinactory:plate_from_ingot", steps.get(0).pattern().patternId());
        assertEquals("tinactory:gear_from_plate", steps.get(1).pattern().patternId());
    }

    @Test
    void plannerShouldUseDeterministicTieBreakByPatternId() {
        var ore = CraftKey.item("tinactory:ore", "");
        var dust = CraftKey.item("tinactory:dust", "");
        var plate = CraftKey.item("tinactory:plate", "");

        var aPattern = pattern(
            "tinactory:a_ore_to_plate",
            List.of(new CraftAmount(ore, 1)),
            List.of(new CraftAmount(plate, 1)));
        var zPattern = pattern(
            "tinactory:z_dust_to_plate",
            List.of(new CraftAmount(dust, 1)),
            List.of(new CraftAmount(plate, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(zPattern, aPattern)));

        var result = planner.plan(
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(ore, 1), new CraftAmount(dust, 1)));

        assertTrue(result.isSuccess());
        assertEquals("tinactory:a_ore_to_plate", result.plan().steps().get(0).pattern().patternId());
    }

    @Test
    void plannerShouldReuseByproductsFromEarlierSteps() {
        var crude = CraftKey.fluid("tinactory:crude_oil", "");
        var plastic = CraftKey.item("tinactory:plastic", "");
        var residue = CraftKey.item("tinactory:residue", "");
        var carbon = CraftKey.item("tinactory:carbon", "");

        var refine = pattern(
            "tinactory:refine_oil",
            List.of(new CraftAmount(crude, 1)),
            List.of(new CraftAmount(plastic, 1), new CraftAmount(residue, 1)));
        var residueToCarbon = pattern(
            "tinactory:residue_to_carbon",
            List.of(new CraftAmount(residue, 1)),
            List.of(new CraftAmount(carbon, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(refine, residueToCarbon)));

        var result = planner.plan(
            List.of(new CraftAmount(plastic, 1), new CraftAmount(carbon, 1)),
            List.of(new CraftAmount(crude, 1)));

        assertTrue(result.isSuccess());
        assertEquals(2, result.plan().steps().size());
        assertEquals(
            List.of("tinactory:refine_oil", "tinactory:residue_to_carbon"),
            result.plan().steps().stream().map(step -> step.pattern().patternId()).toList());
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
