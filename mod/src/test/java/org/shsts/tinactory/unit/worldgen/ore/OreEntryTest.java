package org.shsts.tinactory.unit.worldgen.ore;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class OreEntryTest {
    @Test
    void entryShouldRetainBlockAndWeight() {
        var entry = new OreEntry(modLoc("material/ore/iron"), 3);

        assertEquals(modLoc("material/ore/iron"), entry.block());
        assertEquals(3, entry.weight());
    }

    @Test
    void entryShouldRejectNonPositiveWeight() {
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(modLoc("ore"), 0));
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(modLoc("ore"), -1));
    }

    @Test
    void codecShouldRoundTripEntryWithoutRegistryResolution() {
        var entry = new OreEntry(modLoc("material/ore/iron"), 3);
        var encoded = CodecHelper.encodeTag(TEST_REGISTRY, OreEntry.CODEC, entry);

        assertEquals(entry, CodecHelper.parseTag(TEST_REGISTRY, OreEntry.CODEC, encoded));
    }
}
