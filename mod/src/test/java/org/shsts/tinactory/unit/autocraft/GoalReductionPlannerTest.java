package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;
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
        assertEquals(List.of(new CraftAmount(residue, 1)),
            result.plan().steps().get(0).requiredIntermediateOutputs());
        assertEquals(List.of(new CraftAmount(plastic, 1)),
            result.plan().steps().get(0).requiredFinalOutputs());
        assertEquals(List.of(), result.plan().steps().get(1).requiredIntermediateOutputs());
        assertEquals(List.of(new CraftAmount(carbon, 1)),
            result.plan().steps().get(1).requiredFinalOutputs());
    }

    @Test
    void plannerShouldAggregateDuplicateOutputsByRole() {
        var base = CraftKey.item("tinactory:base", "");
        var part = CraftKey.item("tinactory:part", "");
        var finalKey = CraftKey.item("tinactory:final", "");
        var makePart = pattern(
            "tinactory:make_part",
            List.of(new CraftAmount(base, 2)),
            List.of(new CraftAmount(part, 1), new CraftAmount(part, 1)));
        var makeFinal = pattern(
            "tinactory:make_final",
            List.of(new CraftAmount(part, 1)),
            List.of(new CraftAmount(finalKey, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(makePart, makeFinal)));

        var result = planner.plan(
            List.of(new CraftAmount(finalKey, 1), new CraftAmount(part, 1)),
            List.of(new CraftAmount(base, 2)));

        assertTrue(result.isSuccess());
        var partStep = result.plan().steps().get(0);
        assertEquals(List.of(new CraftAmount(part, 1)), partStep.requiredIntermediateOutputs());
        assertEquals(List.of(new CraftAmount(part, 1)), partStep.requiredFinalOutputs());
    }

    @Test
    void plannerShouldMarkBranchProducerOutputAsIntermediate() {
        var ore = CraftKey.item("tinactory:ore", "");
        var part = CraftKey.item("tinactory:part", "");
        var machineA = CraftKey.item("tinactory:machine_a", "");
        var machineB = CraftKey.item("tinactory:machine_b", "");

        var makePart = pattern(
            "tinactory:part_from_ore",
            List.of(new CraftAmount(ore, 1)),
            List.of(new CraftAmount(part, 2)));
        var makeMachineA = pattern(
            "tinactory:machine_a_from_part",
            List.of(new CraftAmount(part, 1)),
            List.of(new CraftAmount(machineA, 1)));
        var makeMachineB = pattern(
            "tinactory:machine_b_from_part",
            List.of(new CraftAmount(part, 1)),
            List.of(new CraftAmount(machineB, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(makePart, makeMachineA, makeMachineB)));

        var result = planner.plan(
            List.of(new CraftAmount(machineA, 1), new CraftAmount(machineB, 1)),
            List.of(new CraftAmount(ore, 1)));

        assertTrue(result.isSuccess());
        var steps = result.plan().steps();
        assertEquals(List.of(
                "tinactory:part_from_ore",
                "tinactory:machine_a_from_part",
                "tinactory:machine_b_from_part"),
            steps.stream().map($ -> $.pattern().patternId()).toList());
        assertEquals(List.of(new CraftAmount(part, 2)), steps.get(0).requiredIntermediateOutputs());
        assertEquals(List.of(), steps.get(0).requiredFinalOutputs());
    }

    @Test
    void plannerShouldSupportFanInForSharedIntermediateDemand() {
        var ore = CraftKey.item("tinactory:ore", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var part = CraftKey.item("tinactory:part", "");
        var machine = CraftKey.item("tinactory:machine", "");

        var partFromOre = pattern(
            "tinactory:part_from_ore",
            List.of(new CraftAmount(ore, 1)),
            List.of(new CraftAmount(part, 1)));
        var partFromPlate = pattern(
            "tinactory:part_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(part, 1)));
        var machineFromPart = pattern(
            "tinactory:machine_from_part",
            List.of(new CraftAmount(part, 2)),
            List.of(new CraftAmount(machine, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(partFromOre, partFromPlate, machineFromPart)));

        var result = planner.plan(
            List.of(new CraftAmount(machine, 1)),
            List.of(new CraftAmount(ore, 1), new CraftAmount(plate, 1)));

        assertTrue(result.isSuccess());
        var steps = result.plan().steps();
        assertEquals(3, steps.size());
        assertEquals("tinactory:part_from_ore", steps.get(0).pattern().patternId());
        assertEquals("tinactory:part_from_plate", steps.get(1).pattern().patternId());
        assertEquals("tinactory:machine_from_part", steps.get(2).pattern().patternId());
        assertEquals(List.of(new CraftAmount(part, 1)), steps.get(0).requiredIntermediateOutputs());
        assertEquals(List.of(), steps.get(0).requiredFinalOutputs());
        assertEquals(List.of(new CraftAmount(part, 1)), steps.get(1).requiredIntermediateOutputs());
        assertEquals(List.of(), steps.get(1).requiredFinalOutputs());
    }

    @Test
    void plannerShouldMixSecondProducerWhenFirstHasInsufficientInput() {
        var ore = CraftKey.item("tinactory:ore", "");
        var plate = CraftKey.item("tinactory:plate", "");
        var part = CraftKey.item("tinactory:part", "");
        var machine = CraftKey.item("tinactory:machine", "");

        var partFromOre = pattern(
            "tinactory:a_part_from_ore",
            List.of(new CraftAmount(ore, 1)),
            List.of(new CraftAmount(part, 1)));
        var partFromPlate = pattern(
            "tinactory:b_part_from_plate",
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(part, 1)));
        var machineFromPart = pattern(
            "tinactory:machine_from_part",
            List.of(new CraftAmount(part, 3)),
            List.of(new CraftAmount(machine, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(partFromOre, partFromPlate, machineFromPart)));

        var result = planner.plan(
            List.of(new CraftAmount(machine, 1)),
            List.of(new CraftAmount(ore, 2), new CraftAmount(plate, 1)));

        assertTrue(result.isSuccess());
        var steps = result.plan().steps();
        assertEquals(4, steps.size());
        assertEquals("tinactory:a_part_from_ore", steps.get(0).pattern().patternId());
        assertEquals("tinactory:a_part_from_ore", steps.get(1).pattern().patternId());
        assertEquals("tinactory:b_part_from_plate", steps.get(2).pattern().patternId());
        assertEquals("tinactory:machine_from_part", steps.get(3).pattern().patternId());
    }

    @Test
    void plannerShouldBacktrackToSecondRootCandidate() {
        var ore = CraftKey.item("tinactory:ore", "");
        var dust = CraftKey.item("tinactory:dust", "");
        var plate = CraftKey.item("tinactory:plate", "");

        var first = pattern(
            "tinactory:a_plate_from_missing_ore",
            List.of(new CraftAmount(ore, 1)),
            List.of(new CraftAmount(plate, 1)));
        var second = pattern(
            "tinactory:b_plate_from_dust",
            List.of(new CraftAmount(dust, 1)),
            List.of(new CraftAmount(plate, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(first, second)));

        var result = planner.plan(
            List.of(new CraftAmount(plate, 1)),
            List.of(new CraftAmount(dust, 1)));

        assertTrue(result.isSuccess());
        assertEquals(List.of("tinactory:b_plate_from_dust"),
            result.plan().steps().stream().map($ -> $.pattern().patternId()).toList());
    }

    @Test
    void plannerShouldBacktrackNestedCandidate() {
        var ore = CraftKey.item("tinactory:ore", "");
        var dust = CraftKey.item("tinactory:dust", "");
        var ingot = CraftKey.item("tinactory:ingot", "");
        var gear = CraftKey.item("tinactory:gear", "");

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
        var planner = new GoalReductionPlanner(repo(List.of(gearFromIngot, ingotFromOre, ingotFromDust)));

        var result = planner.plan(
            List.of(new CraftAmount(gear, 1)),
            List.of(new CraftAmount(dust, 1)));

        assertTrue(result.isSuccess());
        assertEquals(
            List.of("tinactory:b_ingot_from_dust", "tinactory:gear_from_ingot"),
            result.plan().steps().stream().map($ -> $.pattern().patternId()).toList());
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
