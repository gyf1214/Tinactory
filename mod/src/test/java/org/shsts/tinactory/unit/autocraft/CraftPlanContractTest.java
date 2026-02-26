package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintCodec;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintType;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.model.InputPortConstraint;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.model.OutputPortConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanError;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CraftPlanContractTest {
    @Test
    void craftPlanShouldBeImmutableStepList() {
        var pattern = new CraftPattern(
            "tinactory:gear",
            List.of(new CraftAmount(CraftKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(CraftKey.item("tinactory:gear", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "assembler"), 1, List.of()));
        var step = new CraftStep("step-1", pattern, 3);
        var plan = new CraftPlan(List.of(step));

        assertEquals("step-1", plan.steps().get(0).stepId());
        assertThrows(UnsupportedOperationException.class, () -> plan.steps().add(step));
    }

    @Test
    void craftStepShouldSplitRequiredOutputsByRole() {
        var pattern = new CraftPattern(
            "tinactory:gear",
            List.of(new CraftAmount(CraftKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(CraftKey.item("tinactory:gear", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "assembler"), 1, List.of()));
        var intermediate = List.of(new CraftAmount(CraftKey.item("tinactory:half", ""), 2));
        var finals = List.of(new CraftAmount(CraftKey.item("tinactory:gear", ""), 1));

        var step = new CraftStep("step-1", pattern, 2, intermediate, finals);

        assertEquals(intermediate, step.requiredIntermediateOutputs());
        assertEquals(finals, step.requiredFinalOutputs());
    }

    @Test
    void craftStepShouldDefensivelyCopyRoleOutputs() {
        var pattern = new CraftPattern(
            "tinactory:gear",
            List.of(new CraftAmount(CraftKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(CraftKey.item("tinactory:gear", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "assembler"), 1, List.of()));
        var intermediate = new ArrayList<>(List.of(new CraftAmount(CraftKey.item("tinactory:half", ""), 2)));
        var finals = new ArrayList<>(List.of(new CraftAmount(CraftKey.item("tinactory:gear", ""), 1)));
        var step = new CraftStep("step-1", pattern, 2, intermediate, finals);

        intermediate.clear();
        finals.clear();

        assertEquals(1, step.requiredIntermediateOutputs().size());
        assertEquals(1, step.requiredFinalOutputs().size());
        assertThrows(UnsupportedOperationException.class, () -> step.requiredIntermediateOutputs().add(new CraftAmount(
            CraftKey.item("tinactory:x", ""), 1)));
        assertThrows(UnsupportedOperationException.class, () -> step.requiredFinalOutputs().add(new CraftAmount(
            CraftKey.item("tinactory:y", ""), 1)));
    }

    @Test
    void planErrorShouldExposeTypedPayload() {
        var key = CraftKey.item("tinactory:missing", "");
        var error = PlanError.missingPattern(key);

        assertEquals(PlanError.Code.MISSING_PATTERN, error.code());
        assertEquals(key, error.targetKey());
    }

    @Test
    void machineConstraintRegistryShouldRoundTripByTypeId() {
        var registry = new MachineConstraintRegistry();
        var type = new TestConstraintType();
        var codec = new TestConstraintCodec();
        registry.register(type, codec);

        var decoded = registry.decode("test:constraint", "payload");
        assertEquals("payload", ((TestConstraint) decoded).value());

        var encoded = registry.encode(new TestConstraint("abc"));
        assertEquals("abc", encoded.payload());
        assertEquals("test:constraint", encoded.typeId());
        assertThrows(IllegalArgumentException.class, () -> registry.decode("test:unknown", "x"));
    }

    @Test
    void machineConstraintRegistryShouldPreserveSlotScopedPortConstraints() {
        var registry = new MachineConstraintRegistry();
        registry.register(new InputPortConstraint.Type(), new InputPortConstraint.Codec());
        registry.register(new OutputPortConstraint.Type(), new OutputPortConstraint.Codec());

        var inputConstraint = new InputPortConstraint(1, 4, InputPortConstraint.Direction.INPUT);
        var inputEncoded = registry.encode(inputConstraint);
        var inputDecoded = registry.decode(inputEncoded.typeId(), inputEncoded.payload());
        assertEquals(inputConstraint, inputDecoded);

        var outputConstraint = new OutputPortConstraint(2, 6, OutputPortConstraint.Direction.OUTPUT);
        var outputEncoded = registry.encode(outputConstraint);
        var outputDecoded = registry.decode(outputEncoded.typeId(), outputEncoded.payload());
        assertEquals(outputConstraint, outputDecoded);
    }

    private record TestConstraint(String value) implements IMachineConstraint {
        @Override
        public String typeId() {
            return "test:constraint";
        }
    }

    private static final class TestConstraintType implements IMachineConstraintType<TestConstraint> {
        @Override
        public String id() {
            return "test:constraint";
        }

        @Override
        public Class<TestConstraint> constraintClass() {
            return TestConstraint.class;
        }
    }

    private static final class TestConstraintCodec implements IMachineConstraintCodec<TestConstraint> {
        @Override
        public String encode(TestConstraint constraint) {
            return constraint.value();
        }

        @Override
        public TestConstraint decode(String payload) {
            return new TestConstraint(payload);
        }
    }
}
