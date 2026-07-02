package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternCellData;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatternCellDataTest {
    @Test
    void withPatternShouldReturnImmutableUpdatesAndRemovePatterns() {
        var first = pattern("first");
        var second = pattern("second");

        var data = PatternCellData.EMPTY
            .withPattern(first)
            .withPattern(second);
        var removed = data.withoutPattern(first.patternUuid());

        assertEquals(2, data.patternCount());
        assertEquals(List.of(first, second), List.copyOf(data.patterns()));
        assertEquals(1, removed.patternCount());
        assertFalse(removed.patterns().contains(first));
        assertThrows(UnsupportedOperationException.class, () -> data.patternsById().put(first.patternUuid(), first));
    }

    @Test
    void duplicatePatternInsertionShouldReturnSameInstance() {
        var pattern = pattern("same");
        var data = PatternCellData.EMPTY.withPattern(pattern);

        assertSame(data, data.withPattern(pattern));
    }

    @Test
    void codecShouldRoundTripPatternsAndRecalculateDerivedFields() {
        var first = pattern("first");
        var second = pattern("second");
        var data = PatternCellData.of(Map.of(first.patternUuid(), first, second.patternUuid(), second));
        var patternCodec = new PatternNbtCodec(
            TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            TestStackKey.CODEC).patternCodec();
        var codec = PatternCellData.codec(patternCodec);

        var decoded = CodecHelper.parseTag(codec, CodecHelper.encodeTag(codec, data));

        assertEquals(data, decoded);
        assertEquals(data.hashCode(), decoded.hashCode());
        assertEquals(2, decoded.patternCount());
    }

    private static CraftPattern pattern(String id) {
        return TestAutocraftHelper.pattern(
            id,
            List.of(new CraftAmount(TestStackKey.item("tinactory:" + id + "_input", ""), 1L)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:" + id + "_output", ""), 1L)));
    }
}
