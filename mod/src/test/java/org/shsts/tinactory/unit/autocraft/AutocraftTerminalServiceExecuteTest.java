package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.exec.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPlanPreflight;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewSessionStore;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableKey;
import org.shsts.tinactory.core.autocraft.integration.AutocraftTerminalService;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServiceExecuteTest {
    @Test
    void executeShouldUseStoredSnapshotAndNotInvokePlannerAgain() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        var previewPlanner = new StaticPlanner(planRequiring(
            new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1),
            new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1)));
        var jobService = new AutocraftJobService(cpu,
            (targets, available) -> {
                throw new IllegalStateException("planner should not be called during execute tick");
            },
            TestExecutor::new,
            List::of);
        var service = new AutocraftTerminalService(
            previewPlanner,
            List::of,
            () -> List.copyOf(availableCpus),
            () -> List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 64)),
            new AutocraftPreviewSessionStore(),
            new AutocraftPlanPreflight(),
            id -> id.equals(cpu) ? jobService : null);
        var preview = service.preview(new AutocraftPreviewRequest(
            AutocraftRequestableKey.fromCraftKey(CraftKey.item("minecraft:iron_plate", "")),
            1,
            cpu));

        var execute = service.execute(new AutocraftExecuteRequest(preview.planId(), cpu));

        assertTrue(execute.isSuccess());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
    }

    @Test
    void executeShouldFailWhenPreflightDetectsMissingInputs() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1))),
            List::of,
            () -> List.copyOf(availableCpus),
            List::of,
            new AutocraftPreviewSessionStore(),
            new AutocraftPlanPreflight(),
            id -> new AutocraftJobService(id, (targets, available) -> PlanResult.success(new CraftPlan(List.of())),
                TestExecutor::new, List::of));
        var preview = service.preview(new AutocraftPreviewRequest(
            AutocraftRequestableKey.fromCraftKey(CraftKey.item("minecraft:iron_plate", "")),
            1,
            cpu));

        var execute = service.execute(new AutocraftExecuteRequest(preview.planId(), cpu));

        assertTrue(!execute.isSuccess());
        assertEquals(AutocraftExecuteErrorCode.PREFLIGHT_MISSING_INPUTS, execute.errorCode());
        assertEquals(1L, execute.missingInputs().get(CraftKey.item("minecraft:iron_ingot", "")));
    }

    @Test
    void executeShouldFailWhenCpuBecameUnavailableAfterPreview() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1))),
            List::of,
            () -> List.copyOf(availableCpus),
            () -> List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 64)),
            new AutocraftPreviewSessionStore(),
            new AutocraftPlanPreflight(),
            id -> new AutocraftJobService(id, (targets, available) -> PlanResult.success(new CraftPlan(List.of())),
                TestExecutor::new, List::of));
        var preview = service.preview(new AutocraftPreviewRequest(
            AutocraftRequestableKey.fromCraftKey(CraftKey.item("minecraft:iron_plate", "")),
            1,
            cpu));
        availableCpus.clear();

        var execute = service.execute(new AutocraftExecuteRequest(preview.planId(), cpu));

        assertTrue(!execute.isSuccess());
        assertEquals(AutocraftExecuteErrorCode.CPU_BUSY, execute.errorCode());
    }

    private static CraftPlan planRequiring(CraftAmount input, CraftAmount output) {
        var pattern = new CraftPattern("tinactory:test",
            List.of(input),
            List.of(output),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
        return new CraftPlan(List.of(new CraftStep("s1", pattern, 1L)));
    }

    private static final class StaticPlanner implements ICraftPlanner {
        private final CraftPlan plan;
        private int calls;

        private StaticPlanner(CraftPlan plan) {
            this.plan = plan;
        }

        @Override
        public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
            calls++;
            return PlanResult.success(plan);
        }
    }

    private static final class TestExecutor implements ICraftExecutor {
        @Override
        public void start(CraftPlan plan) {}

        @Override
        public void runCycle(long transmissionBandwidth) {}

        @Override
        public void cancel() {}

        @Override
        public ExecutionState state() {
            return ExecutionState.RUNNING;
        }

        @Override
        public ExecutionError error() {
            return null;
        }

        @Override
        public ExecutionDetails details() {
            return new ExecutionDetails(
                ExecutionDetails.Phase.RUN_STEP, null, null, 0, Map.of(), Map.of(), Map.of(), null);
        }
    }
}
