package org.shsts.tinactory.unit.autocraft;

import com.mojang.serialization.Codec;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.unit.fixture.TestStackKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.autocraft.MachineConstraintCodecHelper;

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
        var codec = Codec.STRING.dispatch(
            IMachineConstraint::typeId,
            id -> switch (id) {
                case "test:constraint" -> Codec.STRING.xmap(TestConstraint::new, TestConstraint::value);
                default -> throw new IllegalArgumentException("unknown machine constraint type id: " + id);
            }
        );

        var decoded = CodecHelper.parseTag(codec, CodecHelper.encodeTag(codec, new TestConstraint("payload")));
        assertEquals("payload", ((TestConstraint) decoded).value());

        var unknown = new CompoundTag();
        unknown.putString("type", "test:unknown");
        unknown.putString("value", "x");
        assertThrows(RuntimeException.class, () -> CodecHelper.parseTag(codec, unknown));
    }

    @Test
    void machineConstraintCodecShouldPreserveSlotScopedPortConstraints() {
        var inputConstraint = new PortConstraint(PortDirection.INPUT, 1, 4);
        var inputOutputDecoded = CodecHelper.parseTag(
            MachineConstraintCodecHelper.CODEC,
            CodecHelper.encodeTag(MachineConstraintCodecHelper.CODEC, inputConstraint));
        assertEquals(inputConstraint, inputOutputDecoded);
    }

    @Test
    void machineConstraintCodecShouldEncodeStructuredPortConstraintPayload() {
        var encoded = (CompoundTag) CodecHelper.encodeTag(
            MachineConstraintCodecHelper.CODEC,
            new PortConstraint(PortDirection.INPUT, 1, 4));

        assertEquals(PortConstraint.TYPE_ID, encoded.getString("type"));
        assertEquals(1, encoded.getInt("slotIndex"));
        assertEquals(4, encoded.getInt("portIndex"));
        assertEquals("input", encoded.getString("direction"));
    }

    private record TestConstraint(String value) implements IMachineConstraint {
        @Override
        public String typeId() {
            return "test:constraint";
        }
    }
}
