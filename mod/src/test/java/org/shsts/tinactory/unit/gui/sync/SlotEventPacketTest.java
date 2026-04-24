package org.shsts.tinactory.unit.gui.sync;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotEventPacketTest {
    @Test
    void roundTripsSlotIndexAndButton() {
        var packet = new SlotEventPacket(27, 1);
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        packet.serializeToBuf(buf);
        var decoded = new SlotEventPacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(27, decoded.getIndex());
        assertEquals(1, decoded.getButton());
    }
}
