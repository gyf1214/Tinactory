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
import org.shsts.tinactory.unit.fixture.TestTechnologyHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestRegistry.TEST_REGISTRY;

class TechnologyTest {
    @Test
    void resolveKeepsOnlyKnownDependencies() {
        var knownLoc = modLoc("known");
        var ignoredLoc = modLoc("ignored");
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
        var displayItem = modLoc("display_item");
        var displayTexture = modLoc("textures/gui/technology/display");
        var jo = new JsonObject();
        jo.addProperty("max_progress", 42L);
        jo.add("modifiers", new JsonObject());
        jo.addProperty("display_item", displayItem.toString());
        jo.addProperty("display_texture", displayTexture.toString());
        jo.addProperty("rank", 9);

        var decoded = CodecHelper.parseJson(TEST_REGISTRY, Technology.CODEC, jo);
        var encoded = CodecHelper.encodeJson(TEST_REGISTRY, Technology.CODEC, decoded).getAsJsonObject();

        assertEquals(new ItemIdRenderDescriptor(displayItem), decoded.getDisplay());
        assertEquals(displayItem.toString(), encoded.get("display_item").getAsString());
        assertEquals(displayTexture.toString(), encoded.get("display_texture").getAsString());
    }

    @Test
    void getDisplayUsesTextureDescriptorWhenNoDisplayItemExists() {
        var displayTexture = modLoc("textures/gui/technology/texture_only");
        var technology = new Technology(List.of(), 20L, Map.of(), Optional.empty(), Optional.of(displayTexture), 1);

        assertEquals(new TextureRenderDescriptor(new Texture(displayTexture, 16, 16)), technology.getDisplay());
    }

    @Test
    void getDisplayFallsBackToEmptyDescriptorWhenDisplayIdsAreMissing() {
        var technology = new Technology(List.of(), 20L, Map.of(), Optional.empty(), Optional.empty(), 1);

        assertEquals(EmptyRenderDescriptor.INSTANCE, technology.getDisplay());
    }

    @Test
    void staticDescriptionAndDetailsIdsShouldFollowConventions() {
        var loc = modLoc("multiblock/large_turbine");

        assertEquals("tinactory.technology.multiblock.large_turbine", Technology.getDescriptionId(loc));
        assertEquals("tinactory.technology.multiblock.large_turbine.details", Technology.getDetailsId(loc));
    }

    private static Technology technology(String loc, List<ResourceLocation> depends, int rank) {
        return TestTechnologyHelper.technology(loc, depends, rank);
    }
}
