package org.shsts.tinactory.unit.autocraft;

import com.mojang.serialization.Codec;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.unit.fixture.TestStackKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.autocraft.MachineConstraintCodecHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatternNbtCodecTest {
    private static final Codec<TestConstraint> TEST_CODEC =
        Codec.STRING.xmap(TestConstraint::new, TestConstraint::value);
    private static final Codec<IMachineConstraint> TEST_CONSTRAINT_CODEC = Codec.STRING.dispatch(
        IMachineConstraint::typeId,
        id -> switch (id) {
            case "test:constraint" -> TEST_CODEC;
            default -> throw new IllegalArgumentException("unknown machine constraint type id: " + id);
        }
    );

    @Test
    void codecShouldRoundTripPattern() {
        var codec = new PatternNbtCodec(TEST_CONSTRAINT_CODEC, TestStackKey.CODEC);
        var pattern = new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", "{x:1b}"), 2)),
            List.of(new CraftAmount(TestStackKey.fluid("minecraft:water", ""), 250)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 2,
                List.of(new TestConstraint("v1"))));

        var tag = codec.encodePattern(pattern);
        var decoded = codec.decodePattern(tag);

        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRejectUnknownConstraintType() {
        var codec = new PatternNbtCodec(TEST_CONSTRAINT_CODEC, TestStackKey.CODEC);
        var tag = new CompoundTag();
        tag.putString("patternId", "tinactory:bad");
        tag.put("inputs", new ListTag());
        var outputs = new ListTag();
        var out = new CompoundTag();
        out.put("key", CodecHelper.encodeTag(
            TestStackKey.CODEC,
            TestStackKey.item("minecraft:iron_ingot", "")));
        out.putLong("amount", 1);
        outputs.add(out);
        tag.put("outputs", outputs);
        var machine = new CompoundTag();
        machine.putString("recipeTypeId", "tinactory:mixer");
        machine.putInt("voltageTier", 0);
        var constraints = new ListTag();
        var constraint = new CompoundTag();
        constraint.putString("type", "missing:type");
        constraint.putString("value", "x");
        constraints.add(constraint);
        machine.put("constraints", constraints);
        tag.put("machineRequirement", machine);

        assertThrows(RuntimeException.class, () -> codec.decodePattern(tag));
    }

    @Test
    void codecShouldRoundTripSlotScopedPortConstraintsWithCpuRegistry() {
        var codec = new PatternNbtCodec(MachineConstraintCodecHelper.CODEC, TestStackKey.CODEC);
        var pattern = new CraftPattern(
            "tinactory:slot_constraints",
            List.of(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:slag", ""), 1)),
            new MachineRequirement(
                new ResourceLocation("tinactory", "press"),
                1,
                List.of(
                    new PortConstraint(PortDirection.INPUT, 0, 2),
                    new PortConstraint(PortDirection.INPUT, 1, null),
                    new PortConstraint(PortDirection.OUTPUT, 0, 5),
                    new PortConstraint(PortDirection.OUTPUT, 1, null))));

        var decoded = codec.decodePattern(codec.encodePattern(pattern));

        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRoundTripCraftAmountWithOverloads() {
        var codec = new PatternNbtCodec(MachineConstraintCodecHelper.CODEC, TestStackKey.CODEC);
        var expected = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", "{foo:1b}"), 17L);

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
}
