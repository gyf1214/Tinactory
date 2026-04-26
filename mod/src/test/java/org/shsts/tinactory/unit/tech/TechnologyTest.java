package org.shsts.tinactory.unit.tech;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.TextureRenderDescriptor;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestTechManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TechnologyTest {
    @Test
    void resolveKeepsOnlyKnownDependencies() {
        var knownLoc = new ResourceLocation("tinactory", "known");
        var ignoredLoc = new ResourceLocation("tinactory", "ignored");
        var known = technology("tinactory:known", List.of(), 1);
        var technology = technology("tinactory:target", List.of(knownLoc, ignoredLoc), 3);

        technology.resolve(new TestTechManager(known));

        assertEquals(List.of(known), technology.getDepends());
    }

    @Test
    void compareToUsesRankWithoutChangingIdentityEquality() {
        var first = technology("tinactory:first", List.of(), 7);
        var second = technology("tinactory:second", List.of(), 7);

        assertEquals(0, first.compareTo(second));
        assertNotEquals(first, second);
    }

    @Test
    void codecRoundTripsDisplayIdsAndPrefersItemDescriptor() {
        var displayItem = new ResourceLocation("tinactory", "display_item");
        var displayTexture = new ResourceLocation("tinactory", "textures/gui/technology/display");
        var json = new JsonObject();
        json.addProperty("max_progress", 42L);
        json.add("modifiers", new JsonObject());
        json.addProperty("display_item", displayItem.toString());
        json.addProperty("display_texture", displayTexture.toString());
        json.addProperty("rank", 9);

        var decoded = CodecHelper.parseJson(Technology.CODEC, json);
        var encoded = CodecHelper.encodeJson(Technology.CODEC, decoded).getAsJsonObject();

        assertEquals(new ItemIdRenderDescriptor(displayItem), decoded.getDisplay());
        assertEquals(displayItem.toString(), encoded.get("display_item").getAsString());
        assertEquals(displayTexture.toString(), encoded.get("display_texture").getAsString());
    }

    @Test
    void getDisplayUsesTextureDescriptorWhenNoDisplayItemExists() {
        var displayTexture = new ResourceLocation("tinactory", "textures/gui/technology/texture_only");
        var technology = new Technology(List.of(), 20L, Map.of(), Optional.empty(), Optional.of(displayTexture), 1);

        assertEquals(new TextureRenderDescriptor(new Texture(displayTexture, 16, 16)), technology.getDisplay());
    }

    @Test
    void getDisplayFallsBackToEmptyDescriptorWhenDisplayIdsAreMissing() {
        var technology = new Technology(List.of(), 20L, Map.of(), Optional.empty(), Optional.empty(), 1);

        assertEquals(EmptyRenderDescriptor.INSTANCE, technology.getDisplay());
    }

    private static Technology technology(String loc, List<ResourceLocation> depends, int rank) {
        var technology = new Technology(depends, 20L, Map.of(), Optional.empty(), Optional.empty(), rank);
        technology.setLoc(new ResourceLocation(loc));
        return technology;
    }
}
