package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServicePreviewTest {

    @Test
    void previewShouldReturnPlan() {
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            List::of,
            List::of,
            List::of,
            List::of);

        var result = service.preview(CraftKey.item("minecraft:iron_ingot", ""), 3);

        assertTrue(result.isSuccess());
        assertEquals(0, result.planSnapshot().steps().size());
        assertEquals(3L, result.targets().get(0).amount());
    }

    private static final class StaticPlanner implements ICraftPlanner {
        @Override
        public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
            return PlanResult.success(new CraftPlan(List.of()));
        }
    }
}
