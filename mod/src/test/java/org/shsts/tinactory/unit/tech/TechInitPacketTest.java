package org.shsts.tinactory.unit.tech;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestBufferHelper;
import org.shsts.tinactory.unit.fixture.TestTechnologyHelper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TechInitPacketTest {
    @Test
    void roundTripsTechnologiesWithUnifiedDisplayAndCompatibleCodecFields() {
        var dependency = technology("tinactory:dependency", List.of(), Optional.empty(), Optional.empty(), 1);
        var technology = technology("tinactory:target", List.of(dependency.loc()),
            Optional.of(new ResourceLocation("tinactory", "display_item")),
            Optional.of(new ResourceLocation("tinactory", "textures/gui/technology/target")), 2);
        var packet = new TechInitPacket(List.of(dependency, technology));
        var buf = TestBufferHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new TechInitPacket();
        decoded.deserializeFromBuf(buf);

        var decodedTechs = decoded.getTechs().stream().toList();
        assertEquals(2, decodedTechs.size());
        assertEquals(dependency.loc(), decodedTechs.get(0).loc());
        assertEquals(technology.loc(), decodedTechs.get(1).loc());
        assertEquals(new ItemIdRenderDescriptor(new ResourceLocation("tinactory", "display_item")),
            decodedTechs.get(1).getDisplay());

        var encoded = CodecHelper.encodeJson(Technology.CODEC, decodedTechs.get(1)).getAsJsonObject();
        assertEquals("tinactory:display_item", encoded.get("display_item").getAsString());
        assertEquals("tinactory:textures/gui/technology/target", encoded.get("display_texture").getAsString());
    }

    private static Technology technology(String loc, List<ResourceLocation> depends,
        Optional<ResourceLocation> displayItem, Optional<ResourceLocation> displayTexture, int rank) {
        return TestTechnologyHelper.technology(loc, depends, displayItem, displayTexture, rank);
    }
}
