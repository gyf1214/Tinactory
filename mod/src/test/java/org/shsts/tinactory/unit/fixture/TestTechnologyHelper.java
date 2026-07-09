package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.tech.Technology;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TestTechnologyHelper {
    private TestTechnologyHelper() {}

    public static Technology technology(String loc, int rank) {
        return technology(loc, List.of(), 20L, Map.of(), Optional.empty(), Optional.empty(), rank);
    }

    public static Technology technology(String loc, List<ResourceLocation> depends, int rank) {
        return technology(loc, depends, 20L, Map.of(), Optional.empty(), Optional.empty(), rank);
    }

    public static Technology technology(String loc, long maxProgress, int rank) {
        return technology(loc, List.of(), maxProgress, Map.of(), Optional.empty(), Optional.empty(), rank);
    }

    public static Technology technology(String loc, List<ResourceLocation> depends, long maxProgress,
        Map<String, Integer> modifiers, int rank) {

        return technology(loc, depends, maxProgress, modifiers, Optional.empty(), Optional.empty(), rank);
    }

    public static Technology technology(String loc, List<ResourceLocation> depends,
        Optional<ResourceLocation> displayItem, Optional<ResourceLocation> displayTexture, int rank) {

        return technology(loc, depends, 20L, Map.of("speed", rank), displayItem, displayTexture, rank);
    }

    public static Technology technology(String loc, List<ResourceLocation> depends, long maxProgress,
        Map<String, Integer> modifiers, Optional<ResourceLocation> displayItem,
        Optional<ResourceLocation> displayTexture, int rank) {

        return new Technology(depends, maxProgress, modifiers, displayItem, displayTexture, rank);
    }

    public static ResourceLocation loc(String loc) {
        return ResourceLocation.parse(loc);
    }
}
