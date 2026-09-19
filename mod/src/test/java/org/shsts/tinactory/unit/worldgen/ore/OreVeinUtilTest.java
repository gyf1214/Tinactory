package org.shsts.tinactory.unit.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.IOreShape;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreShapeInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.ELLIPSOID;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.GOLD_ORE;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.IRON_ORE;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.STONE;

class OreVeinUtilTest {
    @Test
    void hashToUnitShouldPreserveSeedAndPositionFixtures() {
        assertEquals(0.2758902365283412d, OreVeinUtil.hashToUnit(123L, 456L));
        assertEquals(0.7128817798791227d,
            OreVeinUtil.hashToUnit(123L, new BlockPos(10, 20, 30), 456L));
    }

    @Test
    void sampleShouldBeDeterministicAndSampleEachRadiusWithinItsRange() {
        var definition = new OreVeinDefinition(-32, 32,
            shapeDefinition(), 0.75d, STONE, ores());
        var center = new BlockPos(10, 20, 30);
        var first = OreVeinUtil.sample(definition, 123L, center);
        var second = OreVeinUtil.sample(definition, 123L, center);
        var sawNonMinimumRadius = false;

        assertEquals(first, second);
        for (var seed = 0L; seed < 100L; seed++) {
            var instance = OreVeinUtil.sample(definition, seed, center);
            var ellipsoid = ellipsoidInstance(instance);
            var area = ellipsoid.radiusLong() * ellipsoid.radiusShort();
            var eccentricity = (ellipsoid.radiusLong() - ellipsoid.radiusShort()) /
                (ellipsoid.radiusLong() + ellipsoid.radiusShort());
            assertTrue(area >= 100d && area < 200d);
            assertTrue(eccentricity >= 0d && eccentricity < 0.6d);
            assertTrue(ellipsoid.radiusY() >= 1d && ellipsoid.radiusY() < 4d);
            assertTrue(ellipsoid.angle() >= 0d && ellipsoid.angle() < 2d * Math.PI);
            sawNonMinimumRadius |= area > 100d || ellipsoid.radiusY() > 1d;
        }
        assertTrue(sawNonMinimumRadius);
        assertEquals(OreVeinUtil.ALGORITHM_VERSION, first.algorithmVersion());
        assertEquals(definition.hostBlock(), first.hostBlock());
        assertEquals(definition.ores(), first.ores());
    }

    @Test
    void oreAtShouldBeStableRegardlessOfCoordinateIterationOrder() {
        var instance = new OreVeinInstance(
            OreVeinUtil.ALGORITHM_VERSION, 123L, new BlockPos(0, 0, 0),
            shapeInstance(4, 4, 4), 0.6d, STONE,
            List.of(new OreEntry(IRON_ORE, 0.25d), new OreEntry(GOLD_ORE, 0.75d)));
        var forward = new HashMap<BlockPos, Optional<?>>();
        var reverse = new HashMap<BlockPos, Optional<?>>();

        for (var x = -5; x <= 5; x++) {
            for (var y = -5; y <= 5; y++) {
                for (var z = -5; z <= 5; z++) {
                    var position = new BlockPos(x, y, z);
                    forward.put(position, OreVeinUtil.oreAt(instance, position));
                }
            }
        }
        for (var x = 5; x >= -5; x--) {
            for (var y = 5; y >= -5; y--) {
                for (var z = 5; z >= -5; z--) {
                    var position = new BlockPos(x, y, z);
                    reverse.put(position, OreVeinUtil.oreAt(instance, position));
                }
            }
        }

        var results = new HashSet<>(forward.values());
        assertEquals(forward, reverse);
        assertTrue(results.contains(Optional.of(IRON_ORE)));
        assertTrue(results.contains(Optional.of(GOLD_ORE)));
        assertTrue(results.contains(Optional.empty()));
    }

    @Test
    void oreAtShouldApplyEllipsoidBoundsAndDensityFade() {
        var instance = new OreVeinInstance(
            OreVeinUtil.ALGORITHM_VERSION, 321L, new BlockPos(10, 20, 30),
            shapeInstance(3, 2, 3), 1d, STONE, List.of(new OreEntry(IRON_ORE, 1)));
        var bounds = OreVeinUtil.bounds(instance);

        assertEquals(7, bounds.minX());
        assertEquals(18, bounds.minY());
        assertEquals(27, bounds.minZ());
        assertEquals(13, bounds.maxX());
        assertEquals(22, bounds.maxY());
        assertEquals(33, bounds.maxZ());
        assertTrue(OreVeinUtil.oreAt(instance, instance.center()).isPresent());
        assertTrue(OreVeinUtil.oreAt(instance, new BlockPos(13, 20, 30)).isEmpty());
        assertTrue(OreVeinUtil.oreAt(instance, new BlockPos(14, 20, 30)).isEmpty());
        assertFalse(OreVeinUtil.oreAt(instance, new BlockPos(10, 20, 30)).isEmpty());

        for (var x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (var y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (var z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    var position = new BlockPos(x, y, z);
                    if (OreVeinUtil.oreAt(instance, position).isPresent()) {
                        assertTrue(x >= bounds.minX() && x <= bounds.maxX());
                        assertTrue(y >= bounds.minY() && y <= bounds.maxY());
                        assertTrue(z >= bounds.minZ() && z <= bounds.maxZ());
                    }
                }
            }
        }
    }

    @Test
    void oreAtShouldRejectAnInvalidShapeFillFactor() {
        var shape = new InvalidFactorShape();
        var instance = new OreVeinInstance(
            OreVeinUtil.ALGORITHM_VERSION, 1L, new BlockPos(0, 0, 0),
            new OreShapeInstance<>(modLoc("invalid"), shape, 1), 1d, STONE, ores());

        assertThrows(IllegalArgumentException.class, () -> OreVeinUtil.oreAt(instance, new BlockPos(0, 0, 0)));
    }

    @Test
    void intersectingBoundsShouldDelegateThePersistedShapeAndArguments() {
        var shape = new DelegatingShape();
        var center = new BlockPos(10, 20, 30);
        var generationBox = new BoundingBox(1, 2, 3, 4, 5, 6);
        var instance = new OreVeinInstance(
            OreVeinUtil.ALGORITHM_VERSION, 1L, center,
            new OreShapeInstance<>(modLoc("delegate"), shape, 42), 1d, STONE, ores());

        var candidate = OreVeinUtil.intersectingBounds(instance, generationBox);

        assertEquals(center, shape.receivedCenter);
        assertEquals(42, shape.receivedInstance);
        assertEquals(generationBox, shape.receivedGenerationBox);
        assertEquals(Optional.of(new BoundingBox(7, 8, 9, 10, 11, 12)), candidate);

        shape.returnEmpty = true;
        assertEquals(Optional.empty(), OreVeinUtil.intersectingBounds(instance, generationBox));
    }

    private static OreShapeDefinition<EllipsoidShape.Definition> shapeDefinition() {
        return new OreShapeDefinition<>(ELLIPSOID,
            new EllipsoidShape.Definition(100.0, 200.0, 0.6, 1.0, 4.0));
    }

    private static OreShapeInstance<EllipsoidShape.Instance> shapeInstance(
        double radiusLong, double radiusY, double radiusShort) {
        return new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(radiusLong, radiusY, radiusShort, 0));
    }

    private static EllipsoidShape.Instance ellipsoidInstance(OreVeinInstance instance) {
        return (EllipsoidShape.Instance) instance.shape().instance();
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(IRON_ORE, 1d));
    }

    private static final class DelegatingShape implements IOreShape<Integer, Integer> {
        private BlockPos receivedCenter;
        private Integer receivedInstance;
        private BoundingBox receivedGenerationBox;
        private boolean returnEmpty;

        @Override
        public MapCodec<Integer> definitionCodec() {
            return Codec.INT.fieldOf("value");
        }

        @Override
        public MapCodec<Integer> instanceCodec() {
            return Codec.INT.fieldOf("value");
        }

        @Override
        public Integer scaleDefinition(Integer definition, double areaScale) {
            return definition;
        }

        @Override
        public Integer sample(Integer definition, long veinSeed) {
            return definition;
        }

        @Override
        public BoundingBox bounds(BlockPos center, Integer instance) {
            return new BoundingBox(center);
        }

        @Override
        public Optional<BoundingBox> intersectingBounds(BlockPos center, Integer instance,
            BoundingBox generationBox) {
            receivedCenter = center;
            receivedInstance = instance;
            receivedGenerationBox = generationBox;
            return returnEmpty ? Optional.empty() : Optional.of(new BoundingBox(7, 8, 9, 10, 11, 12));
        }

        @Override
        public double fillFactor(long veinSeed, BlockPos center, BlockPos position, Integer instance) {
            return instance;
        }
    }

    private static final class InvalidFactorShape implements IOreShape<Integer, Integer> {
        @Override
        public MapCodec<Integer> definitionCodec() {
            return Codec.INT.fieldOf("value");
        }

        @Override
        public MapCodec<Integer> instanceCodec() {
            return Codec.INT.fieldOf("value");
        }

        @Override
        public Integer scaleDefinition(Integer definition, double areaScale) {
            return definition;
        }

        @Override
        public Integer sample(Integer definition, long veinSeed) {
            return definition;
        }

        @Override
        public BoundingBox bounds(BlockPos center, Integer instance) {
            return new BoundingBox(center);
        }

        @Override
        public Optional<BoundingBox> intersectingBounds(BlockPos center, Integer instance, BoundingBox generationBox) {
            return Optional.of(new BoundingBox(center));
        }

        @Override
        public double fillFactor(long veinSeed, BlockPos center, BlockPos position, Integer instance) {
            return 2d;
        }
    }
}
