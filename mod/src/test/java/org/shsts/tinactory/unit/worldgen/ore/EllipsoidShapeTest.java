package org.shsts.tinactory.unit.worldgen.ore;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class EllipsoidShapeTest {
    private final EllipsoidShape shape = new EllipsoidShape();

    @Test
    void definitionShouldRejectInvalidRanges() {
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(0, 1, 0.6, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(2, 1, 0.6, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 2, -0.1, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 2, 1, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 2, 0.6, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 2, 0.6, 2, 1));
    }

    @Test
    void instanceShouldRejectNonPositiveRadii() {
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, 1, 0));
    }

    @Test
    void definitionAndInstanceCodecsShouldRoundTripThroughJsonAndNbt() {
        var definition = new EllipsoidShape.Definition(100d, 200d, 0.6d, 2d, 5d);
        var instance = new EllipsoidShape.Instance(4.25d, 2.5d, 6.75d);
        var definitionCodec = shape.definitionCodec().codec();
        var instanceCodec = shape.instanceCodec().codec();

        var definitionJson = CodecHelper.encodeJson(TEST_REGISTRY, definitionCodec, definition);
        var definitionTag = CodecHelper.encodeTag(TEST_REGISTRY, definitionCodec, definition);
        var instanceJson = CodecHelper.encodeJson(TEST_REGISTRY, instanceCodec, instance);
        var instanceTag = CodecHelper.encodeTag(TEST_REGISTRY, instanceCodec, instance);

        assertEquals(definition, CodecHelper.parseJson(TEST_REGISTRY, definitionCodec, definitionJson));
        assertEquals(definition, CodecHelper.parseTag(TEST_REGISTRY, definitionCodec, definitionTag));
        assertEquals(instance, CodecHelper.parseJson(TEST_REGISTRY, instanceCodec, instanceJson));
        assertEquals(instance, CodecHelper.parseTag(TEST_REGISTRY, instanceCodec, instanceTag));
    }

    @Test
    void samplingShouldBeDeterministicAndStayWithinConfiguredRanges() {
        var definition = new EllipsoidShape.Definition(100d, 200d, 0.6d, 1d, 4d);
        var first = shape.sample(definition, 123L);
        var second = shape.sample(definition, 123L);

        assertEquals(first, second);
        for (var seed = 0L; seed < 100L; seed++) {
            var instance = shape.sample(definition, seed);
            var area = instance.radiusX() * instance.radiusZ();
            var eccentricity = (instance.radiusX() - instance.radiusZ()) /
                (instance.radiusX() + instance.radiusZ());
            assertTrue(area >= 100d && area < 200d);
            assertTrue(eccentricity >= -0.6d && eccentricity < 0.6d);
            assertTrue(instance.radiusY() >= 1d && instance.radiusY() < 4d);
        }
    }

    @Test
    void samplingShouldDeriveRadiiFromAreaAndEccentricity() {
        var definition = new EllipsoidShape.Definition(100d, 200d, 0.6d, 2d, 5d);

        var instance = shape.sample(definition, 0L);

        assertEquals(21.207360995089804d, instance.radiusX(), 1e-12d);
        assertEquals(4.442336555171429d, instance.radiusY(), 1e-12d);
        assertEquals(5.673043512265947d, instance.radiusZ(), 1e-12d);
        assertEquals(120.3102817054761d, instance.radiusX() * instance.radiusZ(), 1e-12d);
        assertEquals(0.5779049001503281d,
            (instance.radiusX() - instance.radiusZ()) / (instance.radiusX() + instance.radiusZ()), 1e-12d);
    }

    @Test
    void boundsShouldContainTheIntegerLatticePointsOfFractionalRadii() {
        var bounds = shape.bounds(new BlockPos(10, 20, 30), new EllipsoidShape.Instance(2.2d, 1.2d, 3.8d));

        assertEquals(8, bounds.minX());
        assertEquals(19, bounds.minY());
        assertEquals(27, bounds.minZ());
        assertEquals(12, bounds.maxX());
        assertEquals(21, bounds.maxY());
        assertEquals(33, bounds.maxZ());
    }

    @Test
    void fillFactorShouldFadeFromCenterToEdgeAndOutside() {
        var center = new BlockPos(10, 20, 30);
        var instance = new EllipsoidShape.Instance(4, 2, 6);

        assertEquals(1d, shape.fillFactor(123L, center, center, instance));
        assertEquals(0.75d, shape.fillFactor(123L, center, new BlockPos(12, 20, 30), instance));
        assertEquals(0d, shape.fillFactor(123L, center, new BlockPos(14, 20, 30), instance));
        assertEquals(0d, shape.fillFactor(123L, center, new BlockPos(15, 20, 30), instance));
    }

}
