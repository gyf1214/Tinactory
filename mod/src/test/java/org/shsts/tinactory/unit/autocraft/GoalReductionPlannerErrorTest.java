package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import org.shsts.tinactory.unit.fixture.TestInventoryView;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GoalReductionPlannerErrorTest {
    @Test
    void plannerShouldDetectDirectCycle() {
        var a = TestIngredientKey.item("tinactory:a", "");
        var loop = pattern("tinactory:a_from_a", List.of(new CraftAmount(a, 1)), List.of(new CraftAmount(a, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(loop)), TestInventoryView.empty());

        var result = planner.plan(List.of(new CraftAmount(a, 1)));

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.CYCLE_DETECTED, result.error().code());
        assertNotNull(result.error().cyclePath());
        assertEquals(a, result.error().cyclePath().get(0));
    }

    @Test
    void plannerShouldDetectIndirectCycleWithPath() {
        var a = TestIngredientKey.item("tinactory:a", "");
        var b = TestIngredientKey.item("tinactory:b", "");
        var c = TestIngredientKey.item("tinactory:c", "");

        var aFromB = pattern("tinactory:a_from_b", List.of(new CraftAmount(b, 1)), List.of(new CraftAmount(a, 1)));
        var bFromC = pattern("tinactory:b_from_c", List.of(new CraftAmount(c, 1)), List.of(new CraftAmount(b, 1)));
        var cFromA = pattern("tinactory:c_from_a", List.of(new CraftAmount(a, 1)), List.of(new CraftAmount(c, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(aFromB, bFromC, cFromA)), TestInventoryView.empty());

        var result = planner.plan(List.of(new CraftAmount(a, 1)));

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.CYCLE_DETECTED, result.error().code());
        assertEquals(List.of(a, b, c, a), result.error().cyclePath());
    }

    @Test
    void plannerShouldReportMissingPatternForTarget() {
        var missing = TestIngredientKey.item("tinactory:unknown", "");
        var planner = new GoalReductionPlanner(repo(List.of()), TestInventoryView.empty());

        var result = planner.plan(List.of(new CraftAmount(missing, 1)));

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.MISSING_PATTERN, result.error().code());
        assertEquals(missing, result.error().targetKey());
    }

    @Test
    void plannerShouldReportUnsatisfiedBaseResource() {
        var ingot = TestIngredientKey.item("tinactory:ingot", "");
        var gear = TestIngredientKey.item("tinactory:gear", "");
        var gearPattern = pattern(
            "tinactory:gear_from_ingot",
            List.of(new CraftAmount(ingot, 2)),
            List.of(new CraftAmount(gear, 1)));
        var planner = new GoalReductionPlanner(repo(List.of(gearPattern)), TestInventoryView.empty());

        var result = planner.plan(List.of(new CraftAmount(gear, 1)));

        assertEquals(false, result.isSuccess());
        assertEquals(PlanError.Code.UNSATISFIED_BASE_RESOURCE, result.error().code());
        assertEquals(ingot, result.error().targetKey());
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
