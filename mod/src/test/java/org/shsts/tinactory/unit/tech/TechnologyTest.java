package org.shsts.tinactory.unit.tech;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.CodecHelper;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TechnologyTest {
    @Test
    void resolveKeepsOnlyKnownDependencies() {
        var knownLoc = new ResourceLocation("tinactory", "known");
        var ignoredLoc = new ResourceLocation("tinactory", "ignored");
        var known = technology("tinactory:known", List.of(), 1);
        var technology = technology("tinactory:target", List.of(knownLoc, ignoredLoc), 3);

        technology.resolve(new TestTechManager(Map.of(knownLoc, known)));

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
    void codecRoundTripsDisplayIds() {
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

        assertEquals(Optional.of(displayItem), decoded.getDisplayItem());
        assertEquals(Optional.of(displayTexture), decoded.getDisplayTexture());
        assertEquals(displayItem.toString(), encoded.get("display_item").getAsString());
        assertEquals(displayTexture.toString(), encoded.get("display_texture").getAsString());
    }

    private static Technology technology(String loc, List<ResourceLocation> depends, int rank) {
        var technology = new Technology(depends, 20L, Map.of(), Optional.empty(), Optional.empty(), rank);
        technology.setLoc(new ResourceLocation(loc));
        return technology;
    }

    private record TestTechManager(Map<ResourceLocation, Technology> technologies) implements ITechManager {
        @Override
        public Optional<Technology> techByKey(ResourceLocation loc) {
            return Optional.ofNullable(technologies.get(loc));
        }

        @Override
        public Collection<Technology> allTechs() {
            return technologies.values();
        }

        @Override
        public void onProgressChange(Consumer<ITeamProfile> callback) {}

        @Override
        public void removeProgressChangeListener(Consumer<ITeamProfile> callback) {}
    }
}
