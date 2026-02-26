package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteResult;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewResult;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableEntry;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableKey;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalContractTest {
    @Test
    void requestableListShouldDedupByCraftKeyAndType() {
        var itemEntry = new AutocraftRequestableEntry(
            AutocraftRequestableKey.fromCraftKey(CraftKey.item("minecraft:iron_ingot", "")),
            3L);
        var fluidEntry = new AutocraftRequestableEntry(
            AutocraftRequestableKey.fromCraftKey(CraftKey.fluid("minecraft:water", "")),
            2L);

        assertEquals(CraftKey.Type.ITEM, itemEntry.key().type());
        assertEquals(CraftKey.Type.FLUID, fluidEntry.key().type());
    }

    @Test
    void previewResultShouldCarryStablePlanIdAndSnapshot() {
        var planId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var result = AutocraftPreviewResult.success(planId, new CraftPlan(List.of()), List.of(
            new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)));

        assertTrue(result.isSuccess());
        assertEquals(planId, result.planId());
        assertEquals(1, result.summaryOutputs().size());
    }

    @Test
    void executeResultShouldExposePreflightFailureCode() {
        var result = AutocraftExecuteResult.failure(
            AutocraftExecuteErrorCode.PREFLIGHT_MISSING_INPUTS,
            Map.of(CraftKey.item("minecraft:iron_ingot", ""), 4L));

        assertFalse(result.isSuccess());
        assertEquals(AutocraftExecuteErrorCode.PREFLIGHT_MISSING_INPUTS, result.errorCode());
        assertEquals(4L, result.missingInputs().get(CraftKey.item("minecraft:iron_ingot", "")));
    }
}
