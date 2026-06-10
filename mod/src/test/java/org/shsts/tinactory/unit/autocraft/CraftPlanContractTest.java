package org.shsts.tinactory.unit.autocraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CraftPlanContractTest {
    @Test
    void craftPlanShouldBeImmutableStepList() {
        var pattern = new CraftPattern(
            TestAutocraftHelper.uuid("tinactory:gear"),
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)),
            List.of());
        var step = new CraftStep("step-1", pattern, 3);
        var plan = new CraftPlan(List.of(step), PlanSummary.empty(), 0L);

        assertEquals("step-1", plan.steps().get(0).stepId());
        assertThrows(UnsupportedOperationException.class, () -> plan.steps().add(step));
    }

    @Test
    void craftPlanShouldExposeSummaryAndMemoryUsage() {
        var pattern = new CraftPattern(
            TestAutocraftHelper.uuid("tinactory:gear"),
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)),
            List.of());
        var step = new CraftStep("step-1", pattern, 2);
        var summary = new PlanSummary(Map.of(
            TestStackKey.item("tinactory:ingot", ""),
            new PlanSummary.Entry(3L, 2L, 0L),
            TestStackKey.item("tinactory:gear", ""),
            new PlanSummary.Entry(0L, 0L, 2L)));

        var plan = new CraftPlan(List.of(step), summary, 1234L);

        assertEquals(summary, plan.summary());
        assertEquals(1234L, plan.memoryUsage());
    }

    @Test
    void planResultCompletedShouldUsePlanSummary() {
        var pattern = new CraftPattern(
            TestAutocraftHelper.uuid("tinactory:gear"),
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:gear", ""), 1)),
            List.of());
        var steps = new ArrayList<>(List.of(new CraftStep("step-1", pattern, 2)));
        var summary = new PlanSummary(Map.of(
            TestStackKey.item("tinactory:gear", ""),
            new PlanSummary.Entry(0L, 0L, 2L)));

        var plan = new CraftPlan(steps, summary, 4321L);
        steps.clear();
        var result = PlanResult.completed(plan);

        assertEquals(1, plan.steps().size());
        assertThrows(UnsupportedOperationException.class, () -> plan.steps().add(new CraftStep("step-2", pattern, 1)));
        assertEquals(summary, result.summary());
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
        assertEquals(1, encoded.getInt("index"));
        assertEquals(4, encoded.getInt("port"));
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
    void machineConstraintCodecShouldPreserveRecipeTypeConstraint() {
        var constraint = new RecipeTypeConstraint(new ResourceLocation("tinactory", "assembler"));
        var decoded = CodecHelper.parseTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            CodecHelper.encodeTag(TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, constraint));

        assertEquals(constraint, decoded);
    }

    @Test
    void machineConstraintCodecShouldPreserveVoltageConstraint() {
        var constraint = new VoltageConstraint(3);
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

    @Test
    void machineConstraintCodecShouldEncodeStructuredRecipeTypeAndVoltagePayloads() {
        var recipeType = (CompoundTag) CodecHelper.encodeTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            new RecipeTypeConstraint(new ResourceLocation("tinactory", "assembler")));
        var voltage = (CompoundTag) CodecHelper.encodeTag(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            new VoltageConstraint(3));

        assertEquals(RecipeTypeConstraint.TYPE_ID, recipeType.getString("type"));
        assertEquals("tinactory:assembler", recipeType.getString("recipeTypeId"));
        assertEquals(VoltageConstraint.TYPE_ID, voltage.getString("type"));
        assertEquals(3, voltage.getInt("voltageTier"));
    }
}
