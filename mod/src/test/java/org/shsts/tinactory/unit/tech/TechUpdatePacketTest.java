package org.shsts.tinactory.unit.tech;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.unit.fixture.TestBufferHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

class TechUpdatePacketTest {
    @Test
    void roundTripsProgressUpdateWithoutTarget() {
        var packet = TechUpdatePacket.progress(Map.of(
            modLoc("alpha"), 3L,
            modLoc("beta"), 7L));
        var buf = TestBufferHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet.getProgress(), decoded.getProgress());
        assertFalse(decoded.isUpdateTarget());
        assertTrue(decoded.getTargetTech().isEmpty());
    }

    @Test
    void roundTripsFullUpdateWithTarget() {
        var target = modLoc("target");
        var packet = TechUpdatePacket.full(Map.of(target, 11L), target);
        var buf = TestBufferHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertTrue(decoded.isUpdateTarget());
        assertEquals(Map.of(target, 11L), decoded.getProgress());
        assertEquals(target, decoded.getTargetTech().orElseThrow());
    }
}
