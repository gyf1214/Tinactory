package org.shsts.tinactory.unit.common;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.common.WeakMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeakMapTest {
    @Test
    void putAndGetReturnStoredValues() {
        var map = new WeakMap<String, Integer>();
        var ref = map.put("a", 1);

        assertEquals(1, ref.get().orElseThrow());
        assertEquals(1, map.get("a").orElseThrow());
        assertTrue(map.get("missing").isEmpty());
    }

    @Test
    void invalidatedReferencesReadBackAsEmpty() {
        var map = new WeakMap<String, Integer>();
        var ref = map.put("a", 1);

        ref.invalidate();

        assertTrue(ref.get().isEmpty());
        assertTrue(map.get("a").isEmpty());
    }

    @Test
    void replacingReferenceUsesLatestEntryOnly() {
        var map = new WeakMap<String, Integer>();
        var stale = map.put("a", 1);
        var replacement = new WeakMap.Ref<>(2);
        map.put("a", replacement);

        stale.invalidate();

        assertTrue(stale.get().isEmpty());
        assertEquals(2, replacement.get().orElseThrow());
        assertEquals(2, map.get("a").orElseThrow());
        map.clear();
        assertFalse(replacement.get().isEmpty());
        assertTrue(map.get("a").isEmpty());
    }
}
