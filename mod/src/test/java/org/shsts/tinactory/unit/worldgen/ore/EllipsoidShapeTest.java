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
    void definitionShouldRejectInvalidRadiusRanges() {
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(0, 1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(2, 1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 1, 0, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 1, 2, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 1, 1, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Definition(1, 1, 1, 1, 2, 1));
    }

    @Test
    void instanceShouldRejectNonPositiveRadii() {
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EllipsoidShape.Instance(1, 1, 0));
    }

    @Test
    void definitionAndInstanceCodecsShouldRoundTripThroughJsonAndNbt() {
        var definition = new EllipsoidShape.Definition(2, 5, 1, 4, 3, 7);
        var instance = new EllipsoidShape.Instance(4, 2, 6);
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
        var definition = new EllipsoidShape.Definition(2, 5, 1, 4, 3, 7);
        var first = shape.sample(definition, 123L);
        var second = shape.sample(definition, 123L);

        assertEquals(first, second);
        for (var seed = 0L; seed < 100L; seed++) {
            var instance = shape.sample(definition, seed);
            assertTrue(instance.radiusX() >= 2 && instance.radiusX() <= 5);
            assertTrue(instance.radiusY() >= 1 && instance.radiusY() <= 4);
            assertTrue(instance.radiusZ() >= 3 && instance.radiusZ() <= 7);
        }
    }

    @Test
    void samplingShouldPreservePriorRadiusFixtures() {
        var definition = new EllipsoidShape.Definition(2, 5, 1, 4, 3, 7);

        assertEquals(new EllipsoidShape.Instance(2, 4, 7), shape.sample(definition, 0L));
        assertEquals(new EllipsoidShape.Instance(2, 3, 5), shape.sample(definition, 1L));
        assertEquals(new EllipsoidShape.Instance(5, 4, 4), shape.sample(definition, 123L));
        assertEquals(new EllipsoidShape.Instance(3, 1, 7), shape.sample(definition, 12345L));
        assertEquals(new EllipsoidShape.Instance(4, 3, 4), shape.sample(definition, -1L));
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

    @Test
    void boundsShouldClampIntegerOverflow() {
        var maxCenter = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        var minCenter = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        var instance = new EllipsoidShape.Instance(4, 2, 6);

        var maxBounds = shape.bounds(maxCenter, instance);
        var minBounds = shape.bounds(minCenter, instance);

        assertEquals(Integer.MAX_VALUE - 4, maxBounds.minX());
        assertEquals(Integer.MAX_VALUE, maxBounds.maxX());
        assertEquals(Integer.MAX_VALUE - 2, maxBounds.minY());
        assertEquals(Integer.MAX_VALUE, maxBounds.maxY());
        assertEquals(Integer.MAX_VALUE - 6, maxBounds.minZ());
        assertEquals(Integer.MAX_VALUE, maxBounds.maxZ());
        assertEquals(Integer.MIN_VALUE, minBounds.minX());
        assertEquals(Integer.MIN_VALUE + 4, minBounds.maxX());
        assertEquals(Integer.MIN_VALUE, minBounds.minY());
        assertEquals(Integer.MIN_VALUE + 2, minBounds.maxY());
        assertEquals(Integer.MIN_VALUE, minBounds.minZ());
        assertEquals(Integer.MIN_VALUE + 6, minBounds.maxZ());
    }
}
