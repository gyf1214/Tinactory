package org.shsts.tinactory.core.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinUtil {
    public static final int ALGORITHM_VERSION = 1;

    private static final long MAX_SELECTION_WEIGHT = Integer.MAX_VALUE;
    private static final long SELECTION_SALT = 0x4F1BBCDCBFA54001L;
    private static final long RADIUS_X_SALT = 0x9E3779B97F4A7C15L;
    private static final long RADIUS_Y_SALT = 0xD1B54A32D192ED03L;
    private static final long RADIUS_Z_SALT = 0x94D049BB133111EBL;
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
            sampleRadius(veinSeed, definition.minRadiusX(), definition.maxRadiusX(), RADIUS_X_SALT),
            sampleRadius(veinSeed, definition.minRadiusY(), definition.maxRadiusY(), RADIUS_Y_SALT),
            sampleRadius(veinSeed, definition.minRadiusZ(), definition.maxRadiusZ(), RADIUS_Z_SALT),
            definition.density(),
            definition.hostBlock(),
            definition.ores());
    }

    public static Optional<ResourceLocation> oreAt(
        OreVeinInstance instance, BlockPos position) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(position, "position");
        var center = instance.center();
        var dx = (double) position.getX() - center.getX();
        var dy = (double) position.getY() - center.getY();
        var dz = (double) position.getZ() - center.getZ();
        var distance = square(dx / instance.radiusX()) +
            square(dy / instance.radiusY()) +
            square(dz / instance.radiusZ());
        if (distance > 1d) {
            return Optional.empty();
        }
        var fillChance = instance.density() * (1d - distance);
        if (hashToUnit(instance.veinSeed(), position, FILL_SALT) >= fillChance) {
            return Optional.empty();
        }
        return Optional.of(weightedChoice(
            hashToUnit(instance.veinSeed(), position, ORE_SALT), instance.ores()).block());
    }

    public static BoundingBox bounds(OreVeinInstance instance) {
        Objects.requireNonNull(instance, "instance");
        var center = instance.center();
        return new BoundingBox(
            lowerBound(center.getX(), instance.radiusX()),
            lowerBound(center.getY(), instance.radiusY()),
            lowerBound(center.getZ(), instance.radiusZ()),
            upperBound(center.getX(), instance.radiusX()),
            upperBound(center.getY(), instance.radiusY()),
            upperBound(center.getZ(), instance.radiusZ()));
    }

    private static int sampleRadius(long seed, int min, int max, long salt) {
        var range = (long) max - min + 1L;
        return min + (int) (hashToUnit(seed, 0, 0, 0, salt) * range);
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

    private static double square(double value) {
        return value * value;
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

    private static int lowerBound(int center, int radius) {
        return (int) Math.max(Integer.MIN_VALUE, (long) center - radius);
    }

    private static int upperBound(int center, int radius) {
        return (int) Math.min(Integer.MAX_VALUE, (long) center + radius);
    }
}
