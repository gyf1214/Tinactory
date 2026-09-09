package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class EllipsoidShape implements IOreShape<EllipsoidShape.Definition, EllipsoidShape.Instance> {
    private static final long RADIUS_X_SALT = 0x9E3779B97F4A7C15L;
    private static final long RADIUS_Y_SALT = 0xD1B54A32D192ED03L;
    private static final long RADIUS_Z_SALT = 0x94D049BB133111EBL;

    private static final MapCodec<Definition> DEFINITION_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("min_radius_x").forGetter(Definition::minRadiusX),
        Codec.INT.fieldOf("max_radius_x").forGetter(Definition::maxRadiusX),
        Codec.INT.fieldOf("min_radius_y").forGetter(Definition::minRadiusY),
        Codec.INT.fieldOf("max_radius_y").forGetter(Definition::maxRadiusY),
        Codec.INT.fieldOf("min_radius_z").forGetter(Definition::minRadiusZ),
        Codec.INT.fieldOf("max_radius_z").forGetter(Definition::maxRadiusZ)
    ).apply(instance, Definition::new));

    private static final MapCodec<Instance> INSTANCE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("radius_x").forGetter(Instance::radiusX),
        Codec.INT.fieldOf("radius_y").forGetter(Instance::radiusY),
        Codec.INT.fieldOf("radius_z").forGetter(Instance::radiusZ)
    ).apply(instance, Instance::new));

    public EllipsoidShape() {}

    @Override
    public MapCodec<Definition> definitionCodec() {
        return DEFINITION_CODEC;
    }

    @Override
    public MapCodec<Instance> instanceCodec() {
        return INSTANCE_CODEC;
    }

    @Override
    public Instance sample(Definition definition, long veinSeed) {
        return new Instance(
            sampleRadius(veinSeed, definition.minRadiusX(), definition.maxRadiusX(), RADIUS_X_SALT),
            sampleRadius(veinSeed, definition.minRadiusY(), definition.maxRadiusY(), RADIUS_Y_SALT),
            sampleRadius(veinSeed, definition.minRadiusZ(), definition.maxRadiusZ(), RADIUS_Z_SALT));
    }

    @Override
    public BoundingBox bounds(BlockPos center, Instance instance) {
        return new BoundingBox(
            lowerBound(center.getX(), instance.radiusX()),
            lowerBound(center.getY(), instance.radiusY()),
            lowerBound(center.getZ(), instance.radiusZ()),
            upperBound(center.getX(), instance.radiusX()),
            upperBound(center.getY(), instance.radiusY()),
            upperBound(center.getZ(), instance.radiusZ()));
    }

    @Override
    public double fillFactor(long veinSeed, BlockPos center, BlockPos position, Instance instance) {
        var dx = (double) position.getX() - center.getX();
        var dy = (double) position.getY() - center.getY();
        var dz = (double) position.getZ() - center.getZ();
        var distance = square(dx / instance.radiusX()) +
            square(dy / instance.radiusY()) +
            square(dz / instance.radiusZ());
        return distance > 1d ? 0d : 1d - distance;
    }

    private static int sampleRadius(long seed, int min, int max, long salt) {
        var range = (long) max - min + 1L;
        return min + (int) (hashToUnit(seed, salt) * range);
    }

    private static double square(double value) {
        return value * value;
    }

    private static double hashToUnit(long seed, long salt) {
        var hash = seed ^ salt;
        hash = mix64(hash);
        hash = mix64(hash);
        hash = mix64(hash);
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

    public record Definition(
        int minRadiusX,
        int maxRadiusX,
        int minRadiusY,
        int maxRadiusY,
        int minRadiusZ,
        int maxRadiusZ
    ) {
        public Definition {
            validateRadiusRange("X", minRadiusX, maxRadiusX);
            validateRadiusRange("Y", minRadiusY, maxRadiusY);
            validateRadiusRange("Z", minRadiusZ, maxRadiusZ);
        }

        private static void validateRadiusRange(String axis, int min, int max) {
            if (min <= 0 || min > max) {
                throw new IllegalArgumentException("radius " + axis + " range is invalid");
            }
        }
    }

    public record Instance(int radiusX, int radiusY, int radiusZ) {
        public Instance {
            validateRadius("X", radiusX);
            validateRadius("Y", radiusY);
            validateRadius("Z", radiusZ);
        }

        private static void validateRadius(String axis, int radius) {
            if (radius <= 0) {
                throw new IllegalArgumentException("radius " + axis + " must be positive");
            }
        }
    }
}
