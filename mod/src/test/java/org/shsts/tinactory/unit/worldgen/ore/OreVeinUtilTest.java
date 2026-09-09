package org.shsts.tinactory.unit.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;
import org.shsts.tinactory.core.worldgen.ore.shape.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.shape.IOreShape;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.GOLD_ORE;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.HOST;
import static org.shsts.tinactory.unit.fixture.OreBlockTestHelper.IRON_ORE;
import static org.shsts.tinactory.unit.fixture.OreShapeTestHelper.ELLIPSOID;

class OreVeinUtilTest {
    @Test
    void selectShouldRejectAnEmptyDefinitionList() {
        assertThrows(IllegalArgumentException.class, () -> OreVeinUtil.select(List.of(), 1L));
    }

    @Test
    void selectShouldReachFirstMiddleAndLastWeightedDefinitions() {
        var definitions = List.of(
            definition("first", 1), definition("middle", 2), definition("last", 3));
        var selected = new HashSet<String>();
        for (var seed = 0L; seed < 10_000L; seed++) {
            selected.add(OreVeinUtil.select(definitions, seed).id().getPath());
        }

        assertEquals(Set.of("definition/first", "definition/middle", "definition/last"), selected);
    }

    @Test
    void sampleShouldBeDeterministicAndSampleEachRadiusWithinItsRange() {
        var definition = new OreVeinDefinition(
            modLoc("definition"), 1, -32, 32, shapeDefinition(2, 5, 1, 4, 3, 7), 0.75d,
            HOST, ores());
        var center = new BlockPos(10, 20, 30);
        var first = OreVeinUtil.sample(definition, 123L, center);
        var second = OreVeinUtil.sample(definition, 123L, center);
        var sawNonMinimumRadius = false;

        assertEquals(first, second);
        for (var seed = 0L; seed < 100L; seed++) {
            var instance = OreVeinUtil.sample(definition, seed, center);
            var ellipsoid = ellipsoidInstance(instance);
            assertTrue(ellipsoid.radiusX() >= 2 && ellipsoid.radiusX() <= 5);
            assertTrue(ellipsoid.radiusY() >= 1 && ellipsoid.radiusY() <= 4);
            assertTrue(ellipsoid.radiusZ() >= 3 && ellipsoid.radiusZ() <= 7);
            sawNonMinimumRadius |= ellipsoid.radiusX() > 2 || ellipsoid.radiusY() > 1 || ellipsoid.radiusZ() > 3;
        }
        assertTrue(sawNonMinimumRadius);
        assertEquals(OreVeinUtil.ALGORITHM_VERSION, first.algorithmVersion());
        assertEquals(definition.id(), first.definitionId());
        assertEquals(definition.hostBlock(), first.hostBlock());
        assertEquals(definition.ores(), first.ores());
    }

    @Test
    void oreAtShouldBeStableRegardlessOfCoordinateIterationOrder() {
        var instance = new OreVeinInstance(
            OreVeinUtil.ALGORITHM_VERSION, modLoc("definition"), 123L, new BlockPos(0, 0, 0),
            shapeInstance(4, 4, 4), 0.6d, HOST,
            List.of(new OreEntry(IRON_ORE, 1), new OreEntry(GOLD_ORE, 1)));
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
            OreVeinUtil.ALGORITHM_VERSION, modLoc("definition"), 321L, new BlockPos(10, 20, 30),
            shapeInstance(3, 2, 4), 1d, HOST, List.of(new OreEntry(IRON_ORE, 1)));
        var bounds = OreVeinUtil.bounds(instance);

        assertEquals(7, bounds.minX());
        assertEquals(18, bounds.minY());
        assertEquals(26, bounds.minZ());
        assertEquals(13, bounds.maxX());
        assertEquals(22, bounds.maxY());
        assertEquals(34, bounds.maxZ());
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
            OreVeinUtil.ALGORITHM_VERSION, modLoc("definition"), 1L, new BlockPos(0, 0, 0),
            new OreShapeInstance<>(shape, 1), 1d, HOST, ores());

        assertThrows(IllegalArgumentException.class, () -> OreVeinUtil.oreAt(instance, new BlockPos(0, 0, 0)));
    }

    @Test
    void selectShouldRejectTotalWeightOverflow() {
        var huge = definition("huge", Integer.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, () -> OreVeinUtil.select(List.of(huge, huge), 1L));
    }

    private static OreVeinDefinition definition(String path, int selectionWeight) {
        return new OreVeinDefinition(
            modLoc("definition/" + path), selectionWeight, 0, 1, shapeDefinition(1, 1, 1, 1, 1, 1), 1d,
            HOST, ores());
    }

    private static OreShapeDefinition<EllipsoidShape.Definition> shapeDefinition(
        int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        return new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(minX, maxX, minY, maxY, minZ, maxZ));
    }

    private static OreShapeInstance<EllipsoidShape.Instance> shapeInstance(int radiusX, int radiusY, int radiusZ) {
        return new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(radiusX, radiusY, radiusZ));
    }

    private static EllipsoidShape.Instance ellipsoidInstance(OreVeinInstance instance) {
        return (EllipsoidShape.Instance) instance.shape().instance();
    }

    private static List<OreEntry> ores() {
        return List.of(new OreEntry(IRON_ORE, 1));
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
        public Integer sample(Integer definition, long veinSeed) {
            return definition;
        }

        @Override
        public BoundingBox bounds(BlockPos center, Integer instance) {
            return new BoundingBox(center);
        }

        @Override
        public double fillFactor(long veinSeed, BlockPos center, BlockPos position, Integer instance) {
            return 2d;
        }
    }
}
