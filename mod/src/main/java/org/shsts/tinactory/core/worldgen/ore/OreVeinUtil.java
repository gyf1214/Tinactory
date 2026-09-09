package org.shsts.tinactory.core.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.shsts.tinactory.core.worldgen.ore.shape.IOreShape;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeInstance;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinUtil {
    public static final int ALGORITHM_VERSION = 1;

    private static final long MAX_SELECTION_WEIGHT = Integer.MAX_VALUE;
    private static final long SELECTION_SALT = 0x4F1BBCDCBFA54001L;
    private static final long FILL_SALT = 0xD6E8FEB86659FD93L;
    private static final long ORE_SALT = 0xA5A3564E27F2C9B1L;
    private static final long X_HASH = 0x632BE59BD9B4E019L;
    private static final long Y_HASH = 0x8CB92BA72F3D8DD7L;
    private static final long Z_HASH = 0xDB4F0B9175AE2165L;

    private OreVeinUtil() {}

    public static OreVeinDefinition select(List<OreVeinDefinition> definitions, long selectionSeed) {
        Objects.requireNonNull(definitions, "definitions");
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("definitions must not be empty");
        }
        var totalWeight = 0L;
        for (var definition : definitions) {
            Objects.requireNonNull(definition, "definition");
            try {
                totalWeight = Math.addExact(totalWeight, definition.selectionWeight());
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("selection weight total overflowed", exception);
            }
            if (totalWeight > MAX_SELECTION_WEIGHT) {
                throw new IllegalArgumentException("selection weight total is too large");
            }
        }
        var target = (long) (hashToUnit(selectionSeed, 0, 0, 0, SELECTION_SALT) * totalWeight);
        var cumulative = 0L;
        for (var definition : definitions) {
            cumulative += definition.selectionWeight();
            if (target < cumulative) {
                return definition;
            }
        }
        return definitions.get(definitions.size() - 1);
    }

    public static OreVeinInstance sample(OreVeinDefinition definition, long veinSeed, BlockPos center) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(center, "center");
        return new OreVeinInstance(
            ALGORITHM_VERSION,
            definition.id(),
            veinSeed,
            center,
            sampleShape(definition.shape(), veinSeed),
            definition.density(),
            definition.hostBlock(),
            definition.ores());
    }

    public static Optional<Block> oreAt(
        OreVeinInstance instance, BlockPos position) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(position, "position");
        var shapeFactor = fillFactor(instance.shape(), instance.veinSeed(), instance.center(), position);
        if (!Double.isFinite(shapeFactor) || shapeFactor < 0d || shapeFactor > 1d) {
            throw new IllegalArgumentException("shape fill factor must be finite and in the range [0, 1]");
        }
        var fillChance = instance.density() * shapeFactor;
        if (hashToUnit(instance.veinSeed(), position, FILL_SALT) >= fillChance) {
            return Optional.empty();
        }
        return Optional.of(weightedChoice(
            hashToUnit(instance.veinSeed(), position, ORE_SALT), instance.ores()).block());
    }

    public static BoundingBox bounds(OreVeinInstance instance) {
        Objects.requireNonNull(instance, "instance");
        return bounds(instance.shape(), instance.center());
    }

    private static OreShapeInstance<?> sampleShape(OreShapeDefinition<?> definition, long veinSeed) {
        return sampleShapeTyped(definition, veinSeed);
    }

    private static <D> OreShapeInstance<?> sampleShapeTyped(OreShapeDefinition<D> definition, long veinSeed) {
        return sampleShapeTyped(definition, definition.shape(), veinSeed);
    }

    private static <D, I> OreShapeInstance<I> sampleShapeTyped(
        OreShapeDefinition<D> definition, IOreShape<D, I> shape, long veinSeed) {
        return new OreShapeInstance<>(shape, shape.sample(definition.definition(), veinSeed));
    }

    private static double fillFactor(OreShapeInstance<?> instance, long veinSeed, BlockPos center,
        BlockPos position) {
        return fillFactorTyped(instance, veinSeed, center, position);
    }

    private static <I> double fillFactorTyped(OreShapeInstance<I> instance, long veinSeed, BlockPos center,
        BlockPos position) {
        return instance.shape().fillFactor(veinSeed, center, position, instance.instance());
    }

    private static BoundingBox bounds(OreShapeInstance<?> instance, BlockPos center) {
        return boundsTyped(instance, center);
    }

    private static <I> BoundingBox boundsTyped(OreShapeInstance<I> instance, BlockPos center) {
        return instance.shape().bounds(center, instance.instance());
    }

    private static OreEntry weightedChoice(double unit, List<OreEntry> entries) {
        var totalWeight = 0L;
        for (var entry : entries) {
            try {
                totalWeight = Math.addExact(totalWeight, entry.weight());
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("ore weight total overflowed", exception);
            }
        }
        var target = (long) (unit * totalWeight);
        var cumulative = 0L;
        for (var entry : entries) {
            cumulative += entry.weight();
            if (target < cumulative) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    private static double hashToUnit(long seed, BlockPos position, long salt) {
        return hashToUnit(seed, position.getX(), position.getY(), position.getZ(), salt);
    }

    private static double hashToUnit(long seed, int x, int y, int z, long salt) {
        var hash = seed ^ salt;
        hash = mix64(hash + (long) x * X_HASH);
        hash = mix64(hash + (long) y * Y_HASH);
        hash = mix64(hash + (long) z * Z_HASH);
        return (hash >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
