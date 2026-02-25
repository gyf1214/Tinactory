package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewSessionStore;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableKey;
import org.shsts.tinactory.core.autocraft.integration.AutocraftTerminalService;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServicePreviewTest {
    @Test
    void listRequestablesShouldReturnDedupedOutputsFromStoredPatterns() {
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            () -> List.of(
                pattern("tinactory:p1", List.of(
                    new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1))),
                pattern("tinactory:p2", List.of(
                    new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 2),
                    new CraftAmount(CraftKey.fluid("minecraft:water", ""), 1000)))),
            List::of,
            List::of,
            List::of,
            new AutocraftPreviewSessionStore());

        var requestables = service.listRequestables();

        assertEquals(2, requestables.size());
        assertEquals(CraftKey.Type.FLUID, requestables.get(0).key().type());
        assertEquals(CraftKey.Type.ITEM, requestables.get(1).key().type());
        assertEquals(2L, requestables.get(1).producerCount());
    }

    @Test
    void previewShouldRejectUnavailableCpu() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var service = new AutocraftTerminalService(
            new StaticPlanner(),
            List::of,
            List::of,
            () -> List.of(),
            List::of,
            new AutocraftPreviewSessionStore());

        var result = service.preview(new AutocraftPreviewRequest(
            AutocraftRequestableKey.fromCraftKey(CraftKey.item("minecraft:iron_ingot", "")),
            1,
            cpu));

        assertTrue(!result.isSuccess());
        assertEquals(AutocraftPreviewErrorCode.CPU_BUSY, result.errorCode());
    }

    @Test
    void previewShouldPersistImmutablePlanSnapshotByPlanId() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var store = new AutocraftPreviewSessionStore();
        var planner = new StaticPlanner();
        var service = new AutocraftTerminalService(
            planner,
            List::of,
            () -> List.of(cpu),
            () -> List.of(cpu),
            List::of,
            store);

        var result = service.preview(new AutocraftPreviewRequest(
            AutocraftRequestableKey.fromCraftKey(CraftKey.item("minecraft:iron_ingot", "")),
            3,
            cpu));

        assertTrue(result.isSuccess());
        var planId = result.planId();
        var stored = store.find(planId).orElseThrow();
        assertEquals(cpu, stored.cpuId());
        assertEquals(3L, stored.targets().get(0).amount());
        assertEquals(result.planSnapshot(), stored.planSnapshot());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> outputs) {
        return new CraftPattern(id, List.of(
            new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            outputs, new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }

    private static final class StaticPlanner implements ICraftPlanner {
        @Override
        public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
            return PlanResult.success(new CraftPlan(List.of()));
        }
    }
}
