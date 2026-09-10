package org.shsts.tinactory.unit.worldgen.ore;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreShapeInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinInstance;
import org.shsts.tinactory.unit.fixture.TestOreHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.createRegistry;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.ELLIPSOID;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.HOST;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.IRON_ORE;

class OreVeinInstanceTest {
    @Test
    void instanceShouldRetainTheCompleteSampledPayload() {
        var instance = instance();

        assertEquals(1, instance.algorithmVersion());
        assertEquals(modLoc("ore/iron"), instance.definitionId());
        assertEquals(12345L, instance.veinSeed());
        assertEquals(new BlockPos(10, 20, 30), instance.center());
        assertEquals(new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(4, 2, 6)), instance.shape());
        assertEquals(0.75d, instance.density());
        assertEquals(HOST, instance.hostBlock());
        assertEquals(List.of(new OreEntry(IRON_ORE, 3.5d)), instance.ores());
    }

    @Test
    void instanceShouldRejectInvalidAlgorithmRadiiDensityAndComposition() {
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            0, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0.75d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0),
            new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(0, 1, 1)), 0.75d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0),
            new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(1, -1, 1)), 0.75d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 1.1d, HOST, ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0.75d, HOST, List.of()));
    }

    @Test
    void codecShouldRoundTripTheCompleteInstanceThroughBlockRegistry() {
        var instance = instance();
        var registryAccess = createRegistry(TestOreHelper.BLOCKS, TestOreHelper.SHAPES);
        var json = CodecHelper.encodeJson(registryAccess, OreVeinInstance.CODEC, instance);
        var tag = CodecHelper.encodeTag(registryAccess, OreVeinInstance.CODEC, instance);

        assertEquals(instance, CodecHelper.parseJson(registryAccess, OreVeinInstance.CODEC, json));
        assertEquals(instance, CodecHelper.parseTag(registryAccess, OreVeinInstance.CODEC, tag));
    }

    private static OreVeinInstance instance() {
        return new OreVeinInstance(
            1, modLoc("ore/iron"), 12345L, new BlockPos(10, 20, 30), shapeInstance(4, 2, 6), 0.75d,
            HOST, ores());
    }

    private static OreShapeInstance<EllipsoidShape.Instance> shapeInstance() {
        return shapeInstance(1, 1, 1);
    }

    private static OreShapeInstance<EllipsoidShape.Instance> shapeInstance(int radiusX, int radiusY, int radiusZ) {
        return new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(radiusX, radiusY, radiusZ));
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(IRON_ORE, 3.5d));
    }
}
