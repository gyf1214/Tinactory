package org.shsts.tinactory.unit.worldgen.ore;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreVeinInstance;
import org.shsts.tinactory.core.worldgen.ore.shape.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeInstance;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.OreShapeTestHelper.ELLIPSOID;
import static org.shsts.tinactory.unit.fixture.OreShapeTestHelper.SHAPE_CODEC;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

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
        assertEquals(modLoc("host/stone"), instance.hostBlock());
        assertEquals(List.of(new OreEntry(modLoc("ore/iron"), 3)), instance.ores());
    }

    @Test
    void instanceShouldRejectInvalidAlgorithmRadiiDensityAndComposition() {
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            0, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0.75d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), new OreShapeInstance<>(ELLIPSOID,
                new EllipsoidShape.Instance(0, 1, 1)), 0.75d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), new OreShapeInstance<>(ELLIPSOID,
                new EllipsoidShape.Instance(1, -1, 1)), 0.75d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 1.1d, modLoc("host"), ores()));
        assertThrows(IllegalArgumentException.class, () -> new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0.75d, modLoc("host"), List.of()));
    }

    @Test
    void instanceShouldOwnAnImmutableOreList() {
        var ores = new ArrayList<>(ores());
        var instance = new OreVeinInstance(
            1, modLoc("ore"), 1L, new BlockPos(0, 0, 0), shapeInstance(), 0.75d, modLoc("host"), ores);
        ores.clear();

        assertEquals(1, instance.ores().size());
        assertThrows(UnsupportedOperationException.class,
            () -> instance.ores().add(new OreEntry(modLoc("ore/gold"), 1)));
    }

    @Test
    void codecShouldRoundTripTheCompleteInstanceWithoutRegistryResolution() {
        var instance = instance();
        var codec = OreVeinInstance.codec(OreShapeUtil.instanceCodec(SHAPE_CODEC));
        var json = CodecHelper.encodeJson(TEST_REGISTRY, codec.codec(), instance);
        var tag = CodecHelper.encodeTag(TEST_REGISTRY, codec.codec(), instance);

        assertEquals(instance, CodecHelper.parseJson(TEST_REGISTRY, codec.codec(), json));
        assertEquals(instance, CodecHelper.parseTag(TEST_REGISTRY, codec.codec(), tag));
    }

    private static OreVeinInstance instance() {
        return new OreVeinInstance(
            1, modLoc("ore/iron"), 12345L, new BlockPos(10, 20, 30), shapeInstance(4, 2, 6), 0.75d,
            modLoc("host/stone"), ores());
    }

    private static OreShapeInstance<EllipsoidShape.Instance> shapeInstance() {
        return shapeInstance(1, 1, 1);
    }

    private static OreShapeInstance<EllipsoidShape.Instance> shapeInstance(int radiusX, int radiusY, int radiusZ) {
        return new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(radiusX, radiusY, radiusZ));
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(modLoc("ore/iron"), 3));
    }
}
