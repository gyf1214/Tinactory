package org.shsts.tinactory.unit.tech;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.Technology;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TechInitPacketTest {
    @Test
    void roundTripsTechnologiesWithDisplayIds() {
        var dependency = technology("tinactory:dependency", List.of(), Optional.empty(), Optional.empty(), 1);
        var technology = technology("tinactory:target", List.of(dependency.getLoc()),
            Optional.of(new ResourceLocation("tinactory", "display_item")),
            Optional.of(new ResourceLocation("tinactory", "textures/gui/technology/target")), 2);
        var packet = new TechInitPacket(List.of(dependency, technology));
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        packet.serializeToBuf(buf);
        var decoded = new TechInitPacket();
        decoded.deserializeFromBuf(buf);

        var decodedTechs = decoded.getTechs().stream().toList();
        assertEquals(2, decodedTechs.size());
        assertEquals(dependency.getLoc(), decodedTechs.get(0).getLoc());
        assertEquals(technology.getLoc(), decodedTechs.get(1).getLoc());
        assertEquals(technology.getDisplayItem(), decodedTechs.get(1).getDisplayItem());
        assertEquals(technology.getDisplayTexture(), decodedTechs.get(1).getDisplayTexture());
    }

    private static Technology technology(String loc, List<ResourceLocation> depends,
        Optional<ResourceLocation> displayItem, Optional<ResourceLocation> displayTexture, int rank) {

        var technology = new Technology(depends, 20L, Map.of("speed", rank), displayItem, displayTexture, rank);
        technology.setLoc(new ResourceLocation(loc));
        return technology;
    }
}
