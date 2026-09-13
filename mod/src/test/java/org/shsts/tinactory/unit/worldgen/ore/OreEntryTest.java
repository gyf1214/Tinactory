package org.shsts.tinactory.unit.worldgen.ore;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.unit.fixture.TestOreHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.createRegistry;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.IRON_ORE;

class OreEntryTest {
    @Test
    void entryShouldRetainBlockAndWeight() {
        var entry = new OreEntry(IRON_ORE, 3.25d);

        assertEquals(IRON_ORE, entry.block());
        assertEquals(3.25d, entry.weight());
    }

    @Test
    void entryShouldRejectNonPositiveWeight() {
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(IRON_ORE, 0d));
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(IRON_ORE, -0.1d));
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(IRON_ORE, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new OreEntry(IRON_ORE, Double.POSITIVE_INFINITY));
    }

    @Test
    void codecShouldRoundTripEntryThroughBlockRegistry() {
        var entry = new OreEntry(IRON_ORE, 3.25d);
        var registryAccess = createRegistry(TestOreHelper.BLOCKS);
        var encoded = CodecHelper.encodeTag(registryAccess, OreEntry.CODEC, entry);

        assertEquals(entry, CodecHelper.parseTag(registryAccess, OreEntry.CODEC, encoded));
    }
}
