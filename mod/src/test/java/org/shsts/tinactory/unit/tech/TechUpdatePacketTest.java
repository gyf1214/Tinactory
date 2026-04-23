package org.shsts.tinactory.unit.tech;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.tech.TechUpdatePacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechUpdatePacketTest {
    @Test
    void roundTripsProgressUpdateWithoutTarget() {
        var packet = TechUpdatePacket.progress(Map.of(
            new ResourceLocation("tinactory", "alpha"), 3L,
            new ResourceLocation("tinactory", "beta"), 7L));
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet.getProgress(), decoded.getProgress());
        assertFalse(decoded.isUpdateTarget());
        assertTrue(decoded.getTargetTech().isEmpty());
    }

    @Test
    void roundTripsFullUpdateWithTarget() {
        var target = new ResourceLocation("tinactory", "target");
        var packet = TechUpdatePacket.full(Map.of(target, 11L), target);
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        packet.serializeToBuf(buf);
        var decoded = new TechUpdatePacket();
        decoded.deserializeFromBuf(buf);

        assertTrue(decoded.isUpdateTarget());
        assertEquals(Map.of(target, 11L), decoded.getProgress());
        assertEquals(target, decoded.getTargetTech().orElseThrow());
    }
}
