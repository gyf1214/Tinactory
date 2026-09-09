package org.shsts.tinactory.unit.worldgen.ore;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.BLOCK_CODEC;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.HOST;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.IRON_ORE;
import static org.shsts.tinactory.unit.fixture.OreShapeTestHelper.ELLIPSOID;
import static org.shsts.tinactory.unit.fixture.OreShapeTestHelper.SHAPE_CODEC;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class OreVeinDefinitionTest {
    @Test
    void definitionShouldRetainConfiguration() {
        var definition = definition();

        assertEquals(modLoc("ore/iron"), definition.id());
        assertEquals(7, definition.selectionWeight());
        assertEquals(-32, definition.minY());
        assertEquals(64, definition.maxY());
        assertEquals(new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(2, 5, 1, 3, 2, 4)),
            definition.shape());
        assertEquals(0.75d, definition.density());
        assertEquals(HOST, definition.hostBlock());
        assertEquals(List.of(new OreEntry(IRON_ORE, 3)), definition.ores());
    }

    @Test
    void definitionShouldRejectInvalidRangesWeightsDensityAndComposition() {
        assertThrows(IllegalArgumentException.class, () -> definition(0, -32, 64, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> definition(1, 64, -32, 0.75d));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1,
            new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(0, 1, 1, 1, 1, 1)), 0.75d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinDefinition(
            modLoc("ore"), 1, 0, 1,
            new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(2, 1, 1, 1, 1, 1)), 0.75d, HOST, ores()));
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
        var codec = OreVeinDefinition.codec(BLOCK_CODEC, OreVeinUtil.definitionCodec(SHAPE_CODEC));
        var json = CodecHelper.encodeJson(TEST_REGISTRY, codec.codec(), definition);
        var tag = CodecHelper.encodeTag(TEST_REGISTRY, codec.codec(), definition);

        assertEquals(definition, CodecHelper.parseJson(TEST_REGISTRY, codec.codec(), json));
        assertEquals(definition, CodecHelper.parseTag(TEST_REGISTRY, codec.codec(), tag));
    }

    private static OreVeinDefinition definition() {
        return new OreVeinDefinition(
            modLoc("ore/iron"), 7, -32, 64, shapeDefinition(2, 5, 1, 3, 2, 4), 0.75d,
            HOST, ores());
    }

    private static OreVeinDefinition definition(int selectionWeight, int minY, int maxY, double density) {
        return new OreVeinDefinition(
            modLoc("ore"), selectionWeight, minY, maxY, shapeDefinition(), density,
            HOST, ores());
    }

    private static OreShapeDefinition<EllipsoidShape.Definition> shapeDefinition() {
        return shapeDefinition(1, 1, 1, 1, 1, 1);
    }

    private static OreShapeDefinition<EllipsoidShape.Definition> shapeDefinition(
        int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        return new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(minX, maxX, minY, maxY, minZ, maxZ));
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(IRON_ORE, 3));
    }
}
