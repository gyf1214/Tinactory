package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.exec.ExecutionDetails;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.exec.ExecutionState;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftExecuteResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalServiceExecuteTest {
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
            List::of);

        var requestables = service.listRequestables();

        assertEquals(2, requestables.size());
        assertEquals(CraftKey.Type.ITEM, requestables.get(0).type());
        assertEquals(CraftKey.Type.FLUID, requestables.get(1).type());
    }

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
            () -> List.copyOf(availableCpus),
            () -> List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 64)),
            id -> id.equals(cpu) ? jobService : null);

        service.preview(CraftKey.item("minecraft:iron_plate", ""), 1);
        var execute = service.execute(cpu);

        assertTrue(execute.isSuccess());
        assertEquals(1, previewPlanner.calls);
        assertDoesNotThrow(jobService::tick);
    }

    @Test
    void executeShouldFailWhenCpuUnavailable() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var availableCpus = new ArrayList<>(List.of(cpu));
        // TODO
        var jobService = new AutocraftJobService(cpu,
            (targets, available) -> PlanResult.success(new CraftPlan(List.of())),
            TestExecutor::new, List::of) {
            @Override
            public boolean isBusy() {
                return true;
            }
        };
        var service = new AutocraftTerminalService(
            new StaticPlanner(planRequiring(
                new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1))),
            List::of,
            () -> List.copyOf(availableCpus),
            () -> List.copyOf(availableCpus),
            () -> List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 64)),
            id -> id.equals(cpu) ? jobService : null);
        service.preview(CraftKey.item("minecraft:iron_plate", ""), 1);
        availableCpus.clear();

        var execute = service.execute(cpu);

        assertFalse(execute.isSuccess());
        assertEquals(AutocraftExecuteResult.Code.CPU_BUSY, execute.errorCode());
    }

    private static CraftPattern pattern(String id, List<CraftAmount> outputs) {
        return new CraftPattern(id, List.of(
            new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            outputs, new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
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

        private StaticPlanner() {
            this(new CraftPlan(List.of()));
        }

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
