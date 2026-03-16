package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.content.autocraft.AutocraftCpu;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintCodec;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintType;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.InputPortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.OutputPortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatternNbtCodecTest {
    @Test
    void codecShouldRoundTripPattern() {
        var registry = new MachineConstraintRegistry();
        registry.register(new TestConstraintType(), new TestConstraintCodec());
        var codec = new PatternNbtCodec(registry, TestIngredientKey.CODEC);
        var pattern = new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", "{x:1b}"), 2)),
            List.of(new CraftAmount(TestIngredientKey.fluid("minecraft:water", ""), 250)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 2,
                List.of(new TestConstraint("v1"))));

        var tag = codec.encodePattern(pattern);
        var decoded = codec.decodePattern(tag);

        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRejectUnknownConstraintType() {
        var registry = new MachineConstraintRegistry();
        var codec = new PatternNbtCodec(registry, TestIngredientKey.CODEC);
        var tag = new CompoundTag();
        tag.putString("patternId", "tinactory:bad");
        tag.put("inputs", new ListTag());
        var outputs = new ListTag();
        var out = new CompoundTag();
        out.put("key", CodecHelper.encodeTag(
            TestIngredientKey.CODEC,
            TestIngredientKey.item("minecraft:iron_ingot", "")));
        out.putLong("amount", 1);
        outputs.add(out);
        tag.put("outputs", outputs);
        var machine = new CompoundTag();
        machine.putString("recipeTypeId", "tinactory:mixer");
        machine.putInt("voltageTier", 0);
        var constraints = new ListTag();
        var constraint = new CompoundTag();
        constraint.putString("typeId", "missing:type");
        constraint.putString("payload", "x");
        constraints.add(constraint);
        machine.put("constraints", constraints);
        tag.put("machineRequirement", machine);

        assertThrows(IllegalArgumentException.class, () -> codec.decodePattern(tag));
    }

    @Test
    void codecShouldRoundTripSlotScopedPortConstraintsWithCpuRegistry() {
        var codec = new PatternNbtCodec(AutocraftCpu.createConstraintRegistry(), TestIngredientKey.CODEC);
        var pattern = new CraftPattern(
            "tinactory:slot_constraints",
            List.of(
                new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(
                new CraftAmount(TestIngredientKey.item("minecraft:iron_plate", ""), 1),
                new CraftAmount(TestIngredientKey.item("minecraft:slag", ""), 1)),
            new MachineRequirement(
                new ResourceLocation("tinactory", "press"),
                1,
                List.of(
                    new InputPortConstraint(0, 2, null),
                    new InputPortConstraint(1, null, InputPortConstraint.Direction.INPUT),
                    new OutputPortConstraint(0, 5, null),
                    new OutputPortConstraint(1, null, OutputPortConstraint.Direction.OUTPUT))));

        var decoded = codec.decodePattern(codec.encodePattern(pattern));

        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRoundTripCraftAmountWithOverloads() {
        var codec = new PatternNbtCodec(new MachineConstraintRegistry(), TestIngredientKey.CODEC);
        var expected = new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", "{foo:1b}"), 17L);

        var fromAmount = codec.encodeAmount(expected);
        var fromKeyAndAmount = codec.encodeAmount(expected.key(), expected.amount());

        assertEquals(expected, codec.decodeAmount(fromAmount));
        assertEquals(expected, codec.decodeAmount(fromKeyAndAmount));
        assertEquals(fromAmount, fromKeyAndAmount);
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
