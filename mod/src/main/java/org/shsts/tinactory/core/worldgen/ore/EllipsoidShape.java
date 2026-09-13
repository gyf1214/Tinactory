package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Definition scaleDefinition(Definition definition, double areaScale) {
        if (!Double.isFinite(areaScale) || areaScale <= 0d) {
            throw new IllegalArgumentException("area scale must be finite and positive");
        }
        return new Definition(
            definition.minArea() * areaScale,
            definition.maxArea() * areaScale,
            definition.maxEccentric(),
            definition.minRadiusY(),
            definition.maxRadiusY());
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
    public Optional<BoundingBox> intersectingBounds(BlockPos center, Instance instance, BoundingBox generationBox) {
        var overallBounds = bounds(center, instance);
        var minDx = Math.max(generationBox.minX(), overallBounds.minX()) - (double) center.getX();
        var maxDx = Math.min(generationBox.maxX(), overallBounds.maxX()) - (double) center.getX();
        var minDz = Math.max(generationBox.minZ(), overallBounds.minZ()) - (double) center.getZ();
        var maxDz = Math.min(generationBox.maxZ(), overallBounds.maxZ()) - (double) center.getZ();
        if (minDx > maxDx || minDz > maxDz) {
            return Optional.empty();
        }

        var minDy = generationBox.minY() - (double) center.getY();
        var maxDy = generationBox.maxY() - (double) center.getY();
        var dy = Math.max(0d, Math.max(minDy, -maxDy));
        if (dy > instance.radiusY()) {
            return Optional.empty();
        }

        var scaledSquared = Math.max(0d, 1d - square(dy / instance.radiusY()));
        var points = ellipseRectangleIntersections(minDx, maxDx, minDz, maxDz, instance, scaledSquared);
        if (points.isEmpty()) {
            return Optional.empty();
        }

        var minPointX = points.stream().mapToDouble(Point::x).min().orElseThrow();
        var maxPointX = points.stream().mapToDouble(Point::x).max().orElseThrow();
        var minPointZ = points.stream().mapToDouble(Point::z).min().orElseThrow();
        var maxPointZ = points.stream().mapToDouble(Point::z).max().orElseThrow();
        var quadratic = minimumHorizontalQuadratic(minDx, maxDx, minDz, maxDz, instance);
        var verticalRadius = instance.radiusY() * Math.sqrt(Math.max(0d, 1d - quadratic));
        var minY = Math.max(generationBox.minY(), center.getY() - verticalRadius);
        var maxY = Math.min(generationBox.maxY(), center.getY() + verticalRadius);
        var minX = Math.ceil(center.getX() + minPointX);
        var maxX = Math.floor(center.getX() + maxPointX);
        var minZ = Math.ceil(center.getZ() + minPointZ);
        var maxZ = Math.floor(center.getZ() + maxPointZ);
        var roundedMinY = Math.ceil(minY);
        var roundedMaxY = Math.floor(maxY);
        if (minX > maxX || roundedMinY > roundedMaxY || minZ > maxZ) {
            return Optional.empty();
        }
        return Optional.of(new BoundingBox(
            (int) minX, (int) roundedMinY, (int) minZ,
            (int) maxX, (int) roundedMaxY, (int) maxZ));
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

    private static List<Point> ellipseRectangleIntersections(double minDx, double maxDx, double minDz,
        double maxDz, Instance instance, double scaledSquared) {
        var points = new ArrayList<Point>(16);
        var cosine = Math.cos(instance.angle());
        var sine = Math.sin(instance.angle());
        if (scaledSquared == 0d) {
            if (0d >= minDx && 0d <= maxDx && 0d >= minDz && 0d <= maxDz) {
                points.add(new Point(0d, 0d));
            }
            return points;
        }

        var scaledLong = instance.radiusLong() * Math.sqrt(scaledSquared);
        var scaledShort = instance.radiusShort() * Math.sqrt(scaledSquared);
        var coefficientA = square(cosine / scaledLong) + square(sine / scaledShort);
        var coefficientB = sine * cosine * (1d / square(scaledLong) - 1d / square(scaledShort));
        var coefficientC = square(sine / scaledLong) + square(cosine / scaledShort);
        addIfInside(points, minDx, minDz, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB,
            coefficientC);
        addIfInside(points, minDx, maxDz, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB,
            coefficientC);
        addIfInside(points, maxDx, minDz, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB,
            coefficientC);
        addIfInside(points, maxDx, maxDz, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB,
            coefficientC);

        var extentX = Math.sqrt(square(scaledLong * cosine) + square(scaledShort * sine));
        var extentZ = Math.sqrt(square(scaledLong * sine) + square(scaledShort * cosine));
        addIfInside(points, -extentX, 0d, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB, coefficientC);
        addIfInside(points, extentX, 0d, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB, coefficientC);
        addIfInside(points, 0d, -extentZ, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB, coefficientC);
        addIfInside(points, 0d, extentZ, minDx, maxDx, minDz, maxDz, coefficientA, coefficientB, coefficientC);

        addVerticalEdgeRoots(points, minDx, minDz, maxDz, minDx, maxDx, minDz, maxDz, coefficientA,
            coefficientB, coefficientC);
        addVerticalEdgeRoots(points, maxDx, minDz, maxDz, minDx, maxDx, minDz, maxDz, coefficientA,
            coefficientB, coefficientC);
        addHorizontalEdgeRoots(points, minDz, minDx, maxDx, minDx, maxDx, minDz, maxDz, coefficientA,
            coefficientB, coefficientC);
        addHorizontalEdgeRoots(points, maxDz, minDx, maxDx, minDx, maxDx, minDz, maxDz, coefficientA,
            coefficientB, coefficientC);
        return points;
    }

    private static void addVerticalEdgeRoots(List<Point> points, double dx, double minEdgeDz, double maxEdgeDz,
        double minDx, double maxDx, double minDz, double maxDz, double coefficientA, double coefficientB,
        double coefficientC) {
        var discriminant = square(coefficientB * dx) - coefficientC * (coefficientA * square(dx) - 1d);
        addRoots(points, dx, discriminant, -coefficientB * dx, coefficientC, minEdgeDz, maxEdgeDz, minDx,
            maxDx, minDz, maxDz, false);
    }

    private static void addHorizontalEdgeRoots(List<Point> points, double dz, double minEdgeDx, double maxEdgeDx,
        double minDx, double maxDx, double minDz, double maxDz, double coefficientA, double coefficientB,
        double coefficientC) {
        var discriminant = square(coefficientB * dz) - coefficientA * (coefficientC * square(dz) - 1d);
        addRoots(points, dz, discriminant, -coefficientB * dz, coefficientA, minEdgeDx, maxEdgeDx, minDx, maxDx,
            minDz, maxDz, true);
    }

    private static void addRoots(List<Point> points, double fixed, double discriminant, double numerator,
        double denominator, double minVariable, double maxVariable, double minDx, double maxDx, double minDz,
        double maxDz, boolean horizontal) {
        if (discriminant < 0d) {
            return;
        }
        var rootOffset = Math.sqrt(discriminant) / denominator;
        addPoint(points, fixed, numerator / denominator - rootOffset, minVariable, maxVariable, minDx, maxDx, minDz,
            maxDz, horizontal);
        addPoint(points, fixed, numerator / denominator + rootOffset, minVariable, maxVariable, minDx, maxDx, minDz,
            maxDz, horizontal);
    }

    private static void addPoint(List<Point> points, double fixed, double variable, double minVariable,
        double maxVariable, double minDx, double maxDx, double minDz, double maxDz, boolean horizontal) {
        if (variable < minVariable || variable > maxVariable) {
            return;
        }
        var point = horizontal ? new Point(variable, fixed) : new Point(fixed, variable);
        addIfInside(points, point.x(), point.z(), minDx, maxDx, minDz, maxDz, 0d, 0d, 0d);
    }

    private static void addIfInside(List<Point> points, double dx, double dz, double minDx, double maxDx,
        double minDz, double maxDz, double coefficientA, double coefficientB, double coefficientC) {
        if (dx < minDx || dx > maxDx || dz < minDz || dz > maxDz) {
            return;
        }
        if (coefficientA == 0d ||
            coefficientA * square(dx) + 2d * coefficientB * dx * dz + coefficientC * square(dz) <= 1d) {
            points.add(new Point(dx, dz));
        }
    }

    private static double minimumHorizontalQuadratic(double minDx, double maxDx, double minDz, double maxDz,
        Instance instance) {
        var cosine = Math.cos(instance.angle());
        var sine = Math.sin(instance.angle());
        var coefficientA = square(cosine / instance.radiusLong()) + square(sine / instance.radiusShort());
        var coefficientB = sine * cosine *
            (1d / square(instance.radiusLong()) - 1d / square(instance.radiusShort()));
        var coefficientC = square(sine / instance.radiusLong()) + square(cosine / instance.radiusShort());
        if (0d >= minDx && 0d <= maxDx && 0d >= minDz && 0d <= maxDz) {
            return 0d;
        }
        var minimum = Double.POSITIVE_INFINITY;
        for (var dx : new double[] {minDx, maxDx}) {
            var dz = clamp(-coefficientB * dx / coefficientC, minDz, maxDz);
            minimum = Math.min(minimum, quadratic(dx, dz, coefficientA, coefficientB, coefficientC));
        }
        for (var dz : new double[] {minDz, maxDz}) {
            var dx = clamp(-coefficientB * dz / coefficientA, minDx, maxDx);
            minimum = Math.min(minimum, quadratic(dx, dz, coefficientA, coefficientB, coefficientC));
        }
        return Math.max(0d, minimum);
    }

    private static double quadratic(double dx, double dz, double coefficientA, double coefficientB,
        double coefficientC) {
        return coefficientA * square(dx) + 2d * coefficientB * dx * dz + coefficientC * square(dz);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double sampleRange(long seed, double min, double max, long salt) {
        return min + OreVeinUtil.hashToUnit(seed, salt) * (max - min);
    }

    private static double square(double value) {
        return value * value;
    }

    private record Point(double x, double z) {}

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
