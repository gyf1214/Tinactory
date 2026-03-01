package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.logistics.PortTransmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortTransmitterTest {
    @Test
    void shouldProbeTransferWithoutMutation() {
        var from = new TestPort("iron", 10, 8);
        var to = new TestPort("iron", 5, 4);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var moved = transmitter.probe(from, to, new TestStack("iron", 8), 6);

        assertEquals(1, moved.amount());
        assertEquals(8, from.stored);
        assertEquals(4, to.stored);
    }

    @Test
    void shouldSelectFirstTransferableCandidate() {
        var from = new TestPort("iron", 10, 5);
        var to = new TestPort("iron", 10, 0);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var selected = transmitter.select(from, to,
            List.of(new TestStack("gold", 2), new TestStack("iron", 4)), 3);

        assertEquals("iron", selected.id());
        assertEquals(3, selected.amount());
    }

    @Test
    void shouldTransmitAndReturnRemainder() {
        var from = new TestPort("iron", 10, 5);
        var to = new TestPort("iron", 3, 2);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var remainder = transmitter.transmit(from, to, new TestStack("iron", 4));

        assertEquals(1, from.stored);
        assertEquals(3, to.stored);
        assertEquals(3, remainder.amount());
    }
}
