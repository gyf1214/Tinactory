package org.shsts.tinactory.unit.autocraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CraftPlanContractTest {
    @Test
    void craftPlanShouldBeImmutableStepList() {
        var pattern = new CraftPattern(
            "tinactory:gear",
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)),
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
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "assembler"), 1, List.of()));
        var intermediate = List.of(new CraftAmount(TestStackKey.item("tinactory:half", ""), 2));
        var finals = List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1));

        var step = new CraftStep("step-1", pattern, 2, intermediate, finals);

        assertEquals(intermediate, step.requiredIntermediateOutputs());
        assertEquals(finals, step.requiredFinalOutputs());
    }

    @Test
    void craftStepShouldDefensivelyCopyRoleOutputs() {
        var pattern = new CraftPattern(
            "tinactory:gear",
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "assembler"), 1, List.of()));
        var intermediate = new ArrayList<>(List.of(new CraftAmount(TestStackKey.item("tinactory:half", ""), 2)));
        var finals = new ArrayList<>(List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)));
        var step = new CraftStep("step-1", pattern, 2, intermediate, finals);

        intermediate.clear();
        finals.clear();

        assertEquals(1, step.requiredIntermediateOutputs().size());
        assertEquals(1, step.requiredFinalOutputs().size());
        assertThrows(UnsupportedOperationException.class, () -> step.requiredIntermediateOutputs().add(new CraftAmount(
            TestStackKey.item("tinactory:x", ""), 1)));
        assertThrows(UnsupportedOperationException.class, () -> step.requiredFinalOutputs().add(new CraftAmount(
            TestStackKey.item("tinactory:y", ""), 1)));
    }

    @Test
    void planErrorShouldExposeTypedPayload() {
        var key = TestStackKey.item("tinactory:missing", "");
        var error = PlanError.missingPattern(key);

        assertEquals(PlanError.Code.MISSING_PATTERN, error.code());
        assertEquals(key, error.targetKey());
    }

    @Test
    void machineConstraintCodecShouldRoundTripByTypeId() {
        var decoded = CodecHelper.parseTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            CodecHelper.encodeTag(
                TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
                new TestMachineConstraint("payload")));
        assertEquals("payload", ((TestMachineConstraint) decoded).value());

        var unknown = new CompoundTag();
        unknown.putString("type", "test:unknown");
        unknown.putString("value", "x");
        assertThrows(RuntimeException.class,
            () -> CodecHelper.parseTag(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, unknown));
    }

    @Test
    void machineConstraintCodecShouldPreserveSlotScopedPortConstraints() {
        var inputConstraint = new PortConstraint(PortDirection.INPUT, 1, 4);
        var inputOutputDecoded = CodecHelper.parseTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            CodecHelper.encodeTag(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, inputConstraint));
        assertEquals(inputConstraint, inputOutputDecoded);
    }

    @Test
    void machineConstraintCodecShouldEncodeStructuredPortConstraintPayload() {
        var encoded = (CompoundTag) CodecHelper.encodeTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            new PortConstraint(PortDirection.INPUT, 1, 4));

        assertEquals(PortConstraint.TYPE_ID, encoded.getString("type"));
        assertEquals(1, encoded.getInt("slotIndex"));
        assertEquals(4, encoded.getInt("portIndex"));
        assertEquals("input", encoded.getString("direction"));
    }

    @Test
    void machineConstraintCodecShouldPreserveTargetRecipeConstraint() {
        var constraint = new TargetRecipeConstraint(new ResourceLocation("tinactory", "assembler/circuit"));
        var decoded = CodecHelper.parseTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            CodecHelper.encodeTag(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, constraint));

        assertEquals(constraint, decoded);
    }

    @Test
    void machineConstraintCodecShouldEncodeStructuredTargetRecipeConstraintPayload() {
        var encoded = (CompoundTag) CodecHelper.encodeTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            new TargetRecipeConstraint(new ResourceLocation("tinactory", "assembler/circuit")));

        assertEquals(TargetRecipeConstraint.TYPE_ID, encoded.getString("type"));
        assertEquals("tinactory:assembler/circuit", encoded.getString("recipeId"));
    }
}
