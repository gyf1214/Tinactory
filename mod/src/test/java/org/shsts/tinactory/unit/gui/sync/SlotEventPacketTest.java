package org.shsts.tinactory.unit.gui.sync;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotEventPacketTest {
    @Test
    void roundTripsSlotIndexAndButton() {
        var packet = new SlotEventPacket(27, 1);
        var buf = TestCodecHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new SlotEventPacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(27, decoded.getIndex());
        assertEquals(1, decoded.getButton());
    }
}
