package org.shsts.tinactory.unit.tech;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

class TechUpdatePacketTest {
    @Test
    void roundTripsProgressUpdateWithoutTarget() {
        var packet = TechUpdatePacket.incremental("single_player:alpha", Map.of(
            modLoc("alpha"), 3L,
            modLoc("beta"), 7L));
        var buf = TestCodecHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet.getProgress(), decoded.getProgress());
        assertEquals("single_player:alpha", decoded.getProfileId().orElseThrow());
        assertEquals(TechUpdatePacket.UpdateType.INCREMENTAL, decoded.getUpdateType());
        assertFalse(decoded.isUpdateTarget());
        assertTrue(decoded.getTargetTech().isEmpty());
    }

    @Test
    void roundTripsFullUpdateWithTarget() {
        var target = modLoc("target");
        var packet = TechUpdatePacket.full("single_player:alpha", Map.of(target, 11L), target);
        var buf = TestCodecHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertTrue(decoded.isUpdateTarget());
        assertEquals("single_player:alpha", decoded.getProfileId().orElseThrow());
        assertEquals(TechUpdatePacket.UpdateType.FULL, decoded.getUpdateType());
        assertEquals(Map.of(target, 11L), decoded.getProgress());
        assertEquals(target, decoded.getTargetTech().orElseThrow());
    }

    @Test
    void roundTripsClearUpdateWithoutProfile() {
        var packet = TechUpdatePacket.clear();
        var buf = TestCodecHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertTrue(decoded.getProfileId().isEmpty());
        assertEquals(TechUpdatePacket.UpdateType.CLEAR, decoded.getUpdateType());
        assertTrue(decoded.getProgress().isEmpty());
        assertFalse(decoded.isUpdateTarget());
    }
}
