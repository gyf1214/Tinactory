package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PatternRegistryCache;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlannerSnapshot;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServicePreviewTest {

    @Test
    void previewShouldReturnPlan() {
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            new PatternRegistryCache(),
            new TestCpuRuntime());

        var result = service.preview(TestIngredientKey.item("minecraft:iron_ingot", ""), 3);

        assertTrue(result.isSuccess());
        assertEquals(0, result.planSnapshot().steps().size());
        assertEquals(3L, result.targets().get(0).amount());
        assertEquals(PlanError.none(), result.error());
    }

    @Test
    void previewShouldReturnStructuredPlanError() {
        var missing = TestIngredientKey.item("minecraft:iron_ingot", "");
        var service = new AutocraftTerminalService(
            targets -> PlannerSnapshot.failed(PlanError.missingPattern(missing)),
            new PatternRegistryCache(),
            new TestCpuRuntime());

        var result = service.preview(missing, 3);

        assertTrue(!result.isSuccess());
        assertEquals(PlanError.Code.MISSING_PATTERN, result.error().code());
        assertEquals(missing, result.error().targetKey());
    }

    private static final class StaticPlanner implements ICraftPlanner {
        @Override
        public PlannerSnapshot plan(List<CraftAmount> targets) {
            return PlannerSnapshot.completed(new CraftPlan(List.of()));
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
        public List<UUID> listAvailableCpus() {
            return List.of();
        }

        @Override
        public Optional<IAutocraftService> findVisibleService(UUID cpuId) {
            return Optional.empty();
        }
    }
}
