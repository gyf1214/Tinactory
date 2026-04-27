package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.logistics.DigitalProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigitalProviderTest {
    @Test
    void shouldApplyConsumeLimitOffsetAndByteWidth() {
        var provider = new DigitalProvider(20);

        assertEquals(8, provider.consumeLimit(4, 2));
        provider.consume(7);
        assertEquals(4, provider.consumeLimit(5, 2));
        assertEquals(13, provider.consumeLimit(1));
    }

    @Test
    void shouldTrackUsageAndResetCapacity() {
        var provider = new DigitalProvider(16);

        provider.consume(6);
        provider.restore(4);

        assertEquals(2, provider.bytesUsed());
        assertEquals(14, provider.consumeLimit(1));

        provider.reset();

        assertEquals(0, provider.bytesUsed());
        assertEquals(16, provider.consumeLimit(1));
    }
}
