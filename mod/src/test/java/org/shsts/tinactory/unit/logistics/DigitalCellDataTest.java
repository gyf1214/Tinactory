package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.logistics.DigitalCellData;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class DigitalCellDataTest {
    @Test
    void withEntryShouldReturnImmutableUpdatesAndRemoveNonPositiveAmounts() {
        var iron = TestStackKey.item("minecraft:iron_ingot", "");
        var gold = TestStackKey.item("minecraft:gold_ingot", "");

        var data = DigitalCellData.EMPTY
            .withEntry(iron, 5L)
            .withEntry(gold, 7L);
        var removed = data.withEntry(iron, 0L);

        assertEquals(2, data.keyCount());
        assertEquals(12L, data.totalAmount());
        assertEquals(Map.of(iron, 5L, gold, 7L), data.entries());
        assertEquals(1, removed.keyCount());
        assertEquals(7L, removed.totalAmount());
        assertFalse(removed.entries().containsKey(iron));
        assertThrows(UnsupportedOperationException.class, () -> data.entries().put(iron, 1L));
    }

    @Test
    void withEntryShouldReturnSameInstanceWhenAmountIsUnchanged() {
        var iron = TestStackKey.item("minecraft:iron_ingot", "");
        var data = DigitalCellData.EMPTY.withEntry(iron, 5L);

        assertSame(data, data.withEntry(iron, 5L));
        assertNotSame(data, data.withEntry(iron, 6L));
    }

    @Test
    void codecShouldRoundTripEntriesAndRecalculateDerivedFields() {
        var iron = TestStackKey.item("minecraft:iron_ingot", "{foo:1b}");
        var water = TestStackKey.fluid("minecraft:water", "");
        var data = DigitalCellData.of(Map.of(iron, 3L, water, 4000L));
        var codec = DigitalCellData.codec(TestStackKey.CODEC);

        var decoded = CodecHelper.parseTag(TEST_REGISTRY, codec, CodecHelper.encodeTag(TEST_REGISTRY, codec, data));

        assertEquals(data, decoded);
        assertEquals(data.hashCode(), decoded.hashCode());
        assertEquals(2, decoded.keyCount());
        assertEquals(4003L, decoded.totalAmount());
    }
}
