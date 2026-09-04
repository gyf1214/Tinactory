package org.shsts.tinactory.unit.worldgen.ore;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class OreVeinDefinitionTest {
    @Test
    void definitionShouldRetainConfiguration() {
        var definition = definition();

        assertEquals(modLoc("ore/iron"), definition.id());
        assertEquals(7, definition.selectionWeight());
        assertEquals(-32, definition.minY());
        assertEquals(64, definition.maxY());
        assertEquals(2, definition.minRadiusX());
        assertEquals(5, definition.maxRadiusX());
        assertEquals(1, definition.minRadiusY());
        assertEquals(3, definition.maxRadiusY());
        assertEquals(2, definition.minRadiusZ());
        assertEquals(4, definition.maxRadiusZ());
        assertEquals(0.75d, definition.density());
        assertEquals(modLoc("host/stone"), definition.hostBlock());
        assertEquals(List.of(new OreEntry(modLoc("ore/iron"), 3)), definition.ores());
    }

    @Test
    void definitionShouldRejectInvalidRangesWeightsDensityAndComposition() {
        assertThrows(IllegalArgumentException.class, () -> definition(0, -32, 64, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, 64, -32, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1, 0, 1, 1, 1, 1, 1, 0.75d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1, 2, 1, 1, 1, 1, 1, 0.75d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, 0d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, -0.1d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, 1.1d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1, 1, 1, 1, 1, 1, 1, 0.75d, modLoc("host"), List.of()));
        assertThrows(NullPointerException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1, 1, 1, 1, 1, 1, 1, 0.75d, modLoc("host"),
            new ArrayList<>(List.of((OreEntry) null))));
    }

    @Test
    void definitionShouldOwnAnImmutableOreList() {
        var ores = new ArrayList<>(ores());
        var definition = new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1, 1, 1, 1, 1, 1, 1, 0.75d, modLoc("host"), ores);
        ores.clear();

        assertEquals(1, definition.ores().size());
        assertThrows(UnsupportedOperationException.class,
            () -> definition.ores().add(new OreEntry(modLoc("ore/gold"), 1)));
    }

    @Test
    void codecShouldRoundTripDefinitionThroughJsonAndNbtWithoutRegistryResolution() {
        var definition = definition();
        var json = CodecHelper.encodeJson(TEST_REGISTRY, OreVeinDefinition.CODEC, definition);
        var tag = CodecHelper.encodeTag(TEST_REGISTRY, OreVeinDefinition.CODEC, definition);

        assertEquals(definition, CodecHelper.parseJson(TEST_REGISTRY, OreVeinDefinition.CODEC, json));
        assertEquals(definition, CodecHelper.parseTag(TEST_REGISTRY, OreVeinDefinition.CODEC, tag));
    }

    private static OreVeinDefinition definition() {
        return new OreVeinDefinition(
            modLoc("ore/iron"), 7, -32, 64, 2, 5, 1, 3, 2, 4, 0.75d,
            modLoc("host/stone"), ores());
    }

    private static OreVeinDefinition definition(int selectionWeight, int minY, int maxY, double density) {
        return new OreVeinDefinition(
            modLoc("ore"), selectionWeight, minY, maxY, 1, 1, 1, 1, 1, 1, density,
            modLoc("host"), ores());
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(modLoc("ore/iron"), 3));
    }
}
