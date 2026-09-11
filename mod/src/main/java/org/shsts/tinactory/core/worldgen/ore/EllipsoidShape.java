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
    private static final long AREA_SALT = 0x9E3779B97F4A7C15L;
    private static final long RADIUS_Y_SALT = 0xD1B54A32D192ED03L;
    private static final long ECCENTRIC_SALT = 0x94D049BB133111EBL;
    private static final long ANGLE_SALT = 0xE7037ED1A0B428DBL;
    private static final double TWO_PI = 2d * Math.PI;

    private static final MapCodec<Definition> DEFINITION_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.DOUBLE.fieldOf("min_area").forGetter(Definition::minArea),
        Codec.DOUBLE.fieldOf("max_area").forGetter(Definition::maxArea),
        Codec.DOUBLE.fieldOf("max_eccentric").forGetter(Definition::maxEccentric),
        Codec.DOUBLE.fieldOf("min_radius_y").forGetter(Definition::minRadiusY),
        Codec.DOUBLE.fieldOf("max_radius_y").forGetter(Definition::maxRadiusY)
    ).apply(instance, Definition::new));

    private static final MapCodec<Instance> INSTANCE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.DOUBLE.fieldOf("radius_long").forGetter(Instance::radiusLong),
        Codec.DOUBLE.fieldOf("radius_y").forGetter(Instance::radiusY),
        Codec.DOUBLE.fieldOf("radius_short").forGetter(Instance::radiusShort),
        Codec.DOUBLE.fieldOf("angle").forGetter(Instance::angle)
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
        var area = sampleRange(veinSeed, definition.minArea(), definition.maxArea(), AREA_SALT);
        var eccentric = sampleRange(veinSeed, 0d, definition.maxEccentric(), ECCENTRIC_SALT);
        var scale = Math.sqrt(area / (1d - eccentric * eccentric));
        return new Instance(
            scale * (1d + eccentric),
            sampleRange(veinSeed, definition.minRadiusY(), definition.maxRadiusY(), RADIUS_Y_SALT),
            scale * (1d - eccentric),
            sampleRange(veinSeed, 0d, TWO_PI, ANGLE_SALT));
    }

    @Override
    public BoundingBox bounds(BlockPos center, Instance instance) {
        var cosine = Math.cos(instance.angle());
        var sine = Math.sin(instance.angle());
        var extentX = Math.sqrt(square(instance.radiusLong() * cosine) + square(instance.radiusShort() * sine));
        var extentZ = Math.sqrt(square(instance.radiusLong() * sine) + square(instance.radiusShort() * cosine));
        return new BoundingBox(
            (int) Math.ceil(center.getX() - extentX),
            (int) Math.ceil(center.getY() - instance.radiusY()),
            (int) Math.ceil(center.getZ() - extentZ),
            (int) Math.floor(center.getX() + extentX),
            (int) Math.floor(center.getY() + instance.radiusY()),
            (int) Math.floor(center.getZ() + extentZ));
    }

    @Override
    public double fillFactor(long veinSeed, BlockPos center, BlockPos position, Instance instance) {
        var dx = (double) position.getX() - center.getX();
        var dy = (double) position.getY() - center.getY();
        var dz = (double) position.getZ() - center.getZ();
        var cosine = Math.cos(instance.angle());
        var sine = Math.sin(instance.angle());
        var longOffset = dx * cosine + dz * sine;
        var shortOffset = -dx * sine + dz * cosine;
        var distance = square(longOffset / instance.radiusLong()) +
            square(dy / instance.radiusY()) +
            square(shortOffset / instance.radiusShort());
        return distance > 1d ? 0d : 1d - distance;
    }

    private static double sampleRange(long seed, double min, double max, long salt) {
        return min + OreVeinUtil.hashToUnit(seed, salt) * (max - min);
    }

    private static double square(double value) {
        return value * value;
    }

    public record Definition(
        double minArea,
        double maxArea,
        double maxEccentric,
        double minRadiusY,
        double maxRadiusY
    ) {
        public Definition {
            validateRange("area", minArea, maxArea);
            validateRange("Y radius", minRadiusY, maxRadiusY);
            if (!Double.isFinite(maxEccentric) || maxEccentric < 0d || maxEccentric >= 1d) {
                throw new IllegalArgumentException("maximum eccentricity must be finite and in the range [0, 1)");
            }
        }

        private static void validateRange(String name, double min, double max) {
            if (!Double.isFinite(min) || !Double.isFinite(max) || min <= 0d || min > max) {
                throw new IllegalArgumentException(name + " range is invalid");
            }
        }
    }

    public record Instance(double radiusLong, double radiusY, double radiusShort, double angle) {
        public Instance {
            validateRadius("long", radiusLong);
            validateRadius("Y", radiusY);
            validateRadius("short", radiusShort);
            if (radiusLong < radiusShort) {
                throw new IllegalArgumentException("long radius must not be smaller than short radius");
            }
            if (!Double.isFinite(angle) || angle < 0d || angle >= TWO_PI) {
                throw new IllegalArgumentException("angle must be finite and in the range [0, 2π)");
            }
        }

        private static void validateRadius(String axis, double radius) {
            if (!Double.isFinite(radius) || radius <= 0d) {
                throw new IllegalArgumentException("radius " + axis + " must be positive");
            }
        }
    }
}
