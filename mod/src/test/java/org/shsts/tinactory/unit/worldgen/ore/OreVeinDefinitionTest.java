package org.shsts.tinactory.unit.worldgen.ore;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.unit.fixture.TestOreHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.createRegistry;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.ELLIPSOID;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.HOST;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.IRON_ORE;

class OreVeinDefinitionTest {
    @Test
    void definitionShouldRetainConfiguration() {
        var definition = definition();

        assertEquals(modLoc("ore/iron"), definition.id());
        assertEquals(7.25d, definition.selectionWeight());
        assertEquals(-32, definition.minY());
        assertEquals(64, definition.maxY());
        assertEquals(new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(100d, 200d, 0.6d, 1d, 3d)),
            definition.shape());
        assertEquals(0.75d, definition.density());
        assertEquals(HOST, definition.hostBlock());
        assertEquals(List.of(new OreEntry(IRON_ORE, 3.5d)), definition.ores());
    }

    @Test
    void definitionShouldRejectInvalidRangesWeightsDensityAndComposition() {
        assertThrows(IllegalArgumentException.class, () -> definition(0d, -32, 64, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> definition(-0.1d, -32, 64, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> definition(Double.NaN, -32, 64, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> definition(Double.POSITIVE_INFINITY, -32, 64, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> definition(1d, 64, -32, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1d, 0, 1,
            new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(0, 1, 0.6d, 1, 1)), 0.75d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1d, 0, 1,
            new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(2, 1, 0.6d, 1, 1)), 0.75d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, 0d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, -0.1d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, 1.1d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> definition(1, -32, 64, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1, shapeDefinition(), 0.75d, HOST, List.of()));
    }

    @Test
    void codecShouldRoundTripDefinitionThroughBlockRegistry() {
        var definition = definition();
        var registryAccess = createRegistry(TestOreHelper.BLOCKS, TestOreHelper.SHAPES);
        var json = CodecHelper.encodeJson(registryAccess, OreVeinDefinition.CODEC.codec(), definition);
        var tag = CodecHelper.encodeTag(registryAccess, OreVeinDefinition.CODEC.codec(), definition);

        assertEquals(definition, CodecHelper.parseJson(registryAccess, OreVeinDefinition.CODEC.codec(), json));
        assertEquals(definition, CodecHelper.parseTag(registryAccess, OreVeinDefinition.CODEC.codec(), tag));
    }

    private static OreVeinDefinition definition() {
        return new OreVeinDefinition(
            modLoc("ore/iron"), 7.25d, -32, 64, shapeDefinition(100d, 200d, 0.6d, 1d, 3d), 0.75d,
            HOST, ores());
    }

    private static OreVeinDefinition definition(double selectionWeight, int minY, int maxY, double density) {
        return new OreVeinDefinition(
            modLoc("ore"), selectionWeight, minY, maxY, shapeDefinition(), density,
            HOST, ores());
    }

    private static OreShapeDefinition<EllipsoidShape.Definition> shapeDefinition() {
        return shapeDefinition(1d, 1d, 0d, 1d, 1d);
    }

    private static OreShapeDefinition<EllipsoidShape.Definition> shapeDefinition(
        double minArea, double maxArea, double maxEccentric, double minY, double maxY) {
        return new OreShapeDefinition<>(ELLIPSOID,
            new EllipsoidShape.Definition(minArea, maxArea, maxEccentric, minY, maxY));
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(IRON_ORE, 3.5d));
    }
}
