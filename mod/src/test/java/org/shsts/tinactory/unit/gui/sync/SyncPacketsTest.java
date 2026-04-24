package org.shsts.tinactory.unit.gui.sync;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SyncPackets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyncPacketsTest {
    @Test
    void roundTripsDoublePacket() {
        var packet = SyncPackets.doublePacket(3.75D);
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        packet.serializeToBuf(buf);
        var decoded = new SyncPackets.DoublePacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet, decoded);
        assertEquals(3.75D, decoded.getData());
    }

    @Test
    void roundTripsLongPacket() {
        var packet = SyncPackets.longPacket(123456789L);
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        packet.serializeToBuf(buf);
        var decoded = new SyncPackets.LongPacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet, decoded);
        assertEquals(123456789L, decoded.getData());
    }
}
