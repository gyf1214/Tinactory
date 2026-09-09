package org.shsts.tinactory.unit.worldgen.ore;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.BLOCK_CODEC;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.IRON_ORE;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class OreEntryTest {
    @Test
    void entryShouldRetainBlockAndWeight() {
        var entry = new OreEntry(IRON_ORE, 3);

        assertEquals(IRON_ORE, entry.block());
        assertEquals(3, entry.weight());
    }

    @Test
    void entryShouldRejectNonPositiveWeight() {
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(IRON_ORE, 0));
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(IRON_ORE, -1));
    }

    @Test
    void codecShouldRoundTripEntryThroughBlockRegistry() {
        var entry = new OreEntry(IRON_ORE, 3);
        var codec = OreEntry.codec(BLOCK_CODEC);
        var encoded = CodecHelper.encodeTag(TEST_REGISTRY, codec, entry);

        assertEquals(entry, CodecHelper.parseTag(TEST_REGISTRY, codec, encoded));
    }
}
