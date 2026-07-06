package org.shsts.tinactory.unit.autocraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestAutocraftHelper.PATTERN_CODEC;
import static org.shsts.tinactory.unit.fixture.TestAutocraftHelper.PATTERN_CODECS;

class PatternCodecTest {
    private static final UUID TEST_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TARGET_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void codecShouldRoundTripPattern() {
        var pattern = new CraftPattern(
            TEST_UUID,
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", "{x:1b}"), 2)),
            List.of(new CraftAmount(TestStackKey.fluid("minecraft:water", ""), 250)),
            List.of(
                new RecipeTypeConstraint(modLoc("mixer")),
                new VoltageConstraint(2),
                new TestMachineConstraint("v1")));

        var tag = (CompoundTag) PATTERN_CODECS.encodePattern(pattern);
        var decoded = PATTERN_CODECS.decodePattern(tag);

        assertFalse(tag.contains("machineRequirement"));
        assertFalse(tag.contains("recipeTypeId"));
        assertFalse(tag.contains("voltageTier"));
        assertFalse(tag.contains("patternId"));
        assertEquals(TEST_UUID, tag.getUUID("patternUuid"));
        assertEquals(3, tag.getList("constraints", Tag.TAG_COMPOUND).size());
        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRejectUnknownConstraintType() {
        var tag = new CompoundTag();
        tag.putUUID("patternUuid", TEST_UUID);
        tag.put("inputs", new ListTag());
        var outputs = new ListTag();
        var out = new CompoundTag();
        out.put("key", CodecHelper.encodeTag(
            TestStackKey.CODEC,
            TestStackKey.item("minecraft:iron_ingot", "")));
        out.putLong("amount", 1);
        outputs.add(out);
        tag.put("outputs", outputs);
        var constraints = new ListTag();
        var constraint = new CompoundTag();
        constraint.putString("type", "missing:type");
        constraint.putString("value", "x");
        constraints.add(constraint);
        tag.put("constraints", constraints);

        assertThrows(RuntimeException.class, () -> PATTERN_CODECS.decodePattern(tag));
    }

    @Test
    void codecShouldRejectOldPatternIdNbt() {
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
        tag.put("constraints", new ListTag());

        assertThrows(IllegalArgumentException.class, () -> PATTERN_CODECS.decodePattern(tag));
    }

    @Test
    void codecShouldRoundTripSlotScopedPortConstraintsWithCpuRegistry() {
        var pattern = new CraftPattern(
            SLOT_UUID,
            List.of(
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(
                new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1),
                new CraftAmount(TestStackKey.item("minecraft:slag", ""), 1)),
            List.of(
                new PortConstraint(PortDirection.INPUT, 0, 2),
                new PortConstraint(PortDirection.INPUT, 1, 3),
                new PortConstraint(PortDirection.OUTPUT, 0, 5),
                new PortConstraint(PortDirection.OUTPUT, 1, 1)));

        var decoded = PATTERN_CODECS.decodePattern(PATTERN_CODECS.encodePattern(pattern));

        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRoundTripTargetRecipeConstraintWithCpuRegistry() {
        var targetRecipe = new TargetRecipeConstraint(modLoc("assembler/circuit"));
        var pattern = new CraftPattern(
            TARGET_UUID,
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:circuit", ""), 1)),
            List.of(targetRecipe));

        var decoded = PATTERN_CODECS.decodePattern(PATTERN_CODECS.encodePattern(pattern));

        assertEquals(pattern, decoded);
    }

    @Test
    void codecShouldRoundTripCraftAmountWithOverloads() {
        var expected = new CraftAmount(TestStackKey.item("minecraft:iron_ingot", "{foo:1b}"), 17L);

        var fromAmount = PATTERN_CODECS.encodeAmount(expected);
        var fromKeyAndAmount = PATTERN_CODECS.encodeAmount(expected.key(), expected.amount());

        assertEquals(expected, PATTERN_CODECS.decodeAmount(fromAmount));
        assertEquals(expected, PATTERN_CODECS.decodeAmount(fromKeyAndAmount));
        assertEquals(fromAmount, fromKeyAndAmount);
    }

    @Test
    void craftPatternCodecShouldRoundTripPattern() {
        var pattern = new CraftPattern(
            TEST_UUID,
            List.of(new CraftAmount(TestStackKey.item("minecraft:copper_ingot", ""), 2L)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:copper_block", ""), 1L)),
            List.of(new TestMachineConstraint("codec")));

        var tag = CodecHelper.encodeTag(PATTERN_CODEC, pattern);
        var decoded = CodecHelper.parseTag(PATTERN_CODEC, tag);

        assertEquals(pattern, decoded);
    }
}
