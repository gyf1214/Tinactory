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
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(0, 1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, -1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, 1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, 1, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, 1, 1, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, 1, 1, 2d * Math.PI));
    }

    @Test
    void definitionAndInstanceCodecsShouldRoundTripThroughJsonAndNbt() {
        var definition = new EllipsoidShape.Definition(100d, 200d, 0.6d, 2d, 5d);
        var instance = new EllipsoidShape.Instance(6.75d, 2.5d, 4.25d, 1.25d);
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
            var area = instance.radiusLong() * instance.radiusShort();
            var eccentricity = (instance.radiusLong() - instance.radiusShort()) /
                (instance.radiusLong() + instance.radiusShort());
            assertTrue(area >= 100d && area < 200d);
            assertTrue(eccentricity >= 0d && eccentricity < 0.6d);
            assertTrue(instance.radiusY() >= 1d && instance.radiusY() < 4d);
            assertTrue(instance.angle() >= 0d && instance.angle() < 2d * Math.PI);
        }
    }

    @Test
    void samplingShouldDeriveRadiiFromAreaAndEccentricity() {
        var definition = new EllipsoidShape.Definition(120d, 120d, 0.6d, 2d, 2d);

        var instance = shape.sample(definition, 0L);

        var eccentricity = (instance.radiusLong() - instance.radiusShort()) /
            (instance.radiusLong() + instance.radiusShort());
        assertTrue(instance.radiusLong() >= instance.radiusShort());
        assertEquals(120d, instance.radiusLong() * instance.radiusShort(), 1e-12d);
        assertTrue(eccentricity >= 0d && eccentricity < 0.6d);
        assertEquals(2d, instance.radiusY());
        assertTrue(instance.angle() >= 0d && instance.angle() < 2d * Math.PI);
    }

    @Test
    void boundsShouldContainTheIntegerLatticePointsOfFractionalRadii() {
        var bounds = shape.bounds(new BlockPos(10, 20, 30), new EllipsoidShape.Instance(3.8d, 1.2d, 2.2d, 0d));

        assertEquals(7, bounds.minX());
        assertEquals(19, bounds.minY());
        assertEquals(28, bounds.minZ());
        assertEquals(13, bounds.maxX());
        assertEquals(21, bounds.maxY());
        assertEquals(32, bounds.maxZ());
    }

    @Test
    void boundsShouldRotateHorizontalExtents() {
        var center = new BlockPos(10, 20, 30);

        var alongX = shape.bounds(center, new EllipsoidShape.Instance(4d, 2d, 2d, 0d));
        assertEquals(6, alongX.minX());
        assertEquals(18, alongX.minY());
        assertEquals(28, alongX.minZ());
        assertEquals(14, alongX.maxX());
        assertEquals(22, alongX.maxY());
        assertEquals(32, alongX.maxZ());

        var alongZ = shape.bounds(center, new EllipsoidShape.Instance(4d, 2d, 2d, Math.PI / 2d));
        assertEquals(8, alongZ.minX());
        assertEquals(18, alongZ.minY());
        assertEquals(26, alongZ.minZ());
        assertEquals(12, alongZ.maxX());
        assertEquals(22, alongZ.maxY());
        assertEquals(34, alongZ.maxZ());
    }

    @Test
    void fillFactorShouldFadeFromCenterToEdgeAndOutside() {
        var center = new BlockPos(10, 20, 30);
        var instance = new EllipsoidShape.Instance(4, 2, 4, 0d);

        assertEquals(1d, shape.fillFactor(123L, center, center, instance));
        assertEquals(0.75d, shape.fillFactor(123L, center, new BlockPos(12, 20, 30), instance));
        assertEquals(0d, shape.fillFactor(123L, center, new BlockPos(14, 20, 30), instance));
        assertEquals(0d, shape.fillFactor(123L, center, new BlockPos(15, 20, 30), instance));
    }

    @Test
    void fillFactorShouldUseTheRotatedLongAndShortAxes() {
        var center = new BlockPos(10, 20, 30);
        var instance = new EllipsoidShape.Instance(4, 2, 1, Math.PI / 2d);

        assertEquals(0.75d, shape.fillFactor(123L, center, new BlockPos(10, 20, 32), instance));
        assertEquals(0d, shape.fillFactor(123L, center, new BlockPos(11, 20, 30), instance));
    }

}
