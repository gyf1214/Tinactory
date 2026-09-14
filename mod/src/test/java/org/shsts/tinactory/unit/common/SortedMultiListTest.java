package org.shsts.tinactory.unit.common;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.common.SortedMultiList;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SortedMultiListTest {
    @Test
    void shouldTrackEveryInsertedOccurrenceForAnExistingKey() {
        var values = new SortedMultiList<String>(Comparator.naturalOrder());

        values.insert("iron", 2);
        values.insert("iron", 3);

        assertEquals(5, values.size());
        assertEquals(new SortedMultiList.IndexedValue<>("iron", 0, 5), values.get(0));
        assertEquals(new SortedMultiList.IndexedValue<>("iron", 4, 5), values.get(4));
        assertNull(values.get(5));
    }

    @Test
    void shouldKeepOccurrencesSortedAndRemoveOnlyRequestedCount() {
        var values = new SortedMultiList<String>(Comparator.naturalOrder());

        values.insert("gold", 2);
        values.insert("iron", 3);

        assertEquals("gold", values.get(0).value());
        assertEquals("gold", values.get(1).value());
        assertEquals("iron", values.get(2).value());

        values.remove("gold", 1);

        assertEquals(4, values.size());
        assertEquals("gold", values.get(0).value());
        assertEquals("iron", values.get(1).value());
    }

    @Test
    void shouldKeepComparatorEquivalentKeysDistinctAfterRemovingAndReinserting() {
        var values = new SortedMultiList<String>(Comparator.comparingInt(String::length));

        values.insert("a", 1);
        values.insert("b", 1);
        values.remove("a", 1);
        values.insert("c", 1);

        assertEquals(2, values.size());
        assertEquals("b", values.get(0).value());
        assertEquals("c", values.get(1).value());
    }
}
