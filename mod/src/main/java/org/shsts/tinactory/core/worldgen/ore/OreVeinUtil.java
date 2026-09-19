package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

import static org.shsts.tinactory.AllRegistries.ORE_SHAPES;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinUtil {
    public static final int ALGORITHM_VERSION = 3;
    private static final Codec<IEntry<IOreShape<?, ?>>> SHAPE_CODEC = CodecHelper.entryCodec(ORE_SHAPES.key());
    public static final MapCodec<OreShapeDefinition<?>> DEFINITION_CODEC =
        SHAPE_CODEC.dispatchMap(OreShapeDefinition::entry, OreVeinUtil::definitionCodecFor);
    public static final MapCodec<OreShapeInstance<?>> INSTANCE_CODEC =
        SHAPE_CODEC.dispatchMap(OreShapeInstance::entry, OreVeinUtil::instanceCodecFor);

    private static final long FILL_SALT = 0xD6E8FEB86659FD93L;
    private static final long ORE_SALT = 0xA5A3564E27F2C9B1L;
    private static final long X_HASH = 0x632BE59BD9B4E019L;
    private static final long Y_HASH = 0x8CB92BA72F3D8DD7L;
    private static final long Z_HASH = 0xDB4F0B9175AE2165L;

    private OreVeinUtil() {}

    public static OreVeinInstance sample(OreVeinDefinition definition, long veinSeed, BlockPos center) {
        return new OreVeinInstance(
            ALGORITHM_VERSION,
            veinSeed,
            center,
            sampleShape(definition.shape(), veinSeed),
            definition.density(),
            definition.hostBlock(),
            definition.ores());
    }

    public static Optional<Block> oreAt(OreVeinInstance instance, BlockPos position) {
        var shapeFactor = fillFactor(instance.shape(), instance.veinSeed(), instance.center(), position);
        if (!Double.isFinite(shapeFactor) || shapeFactor < 0d || shapeFactor > 1d) {
            throw new IllegalArgumentException("shape fill factor must be finite and in the range [0, 1]");
        }
        var fillChance = instance.density() * shapeFactor;
        if (hashToUnit(instance.veinSeed(), position, FILL_SALT) >= fillChance) {
            return Optional.empty();
        }
        var oreChoice = hashToUnit(instance.veinSeed(), position, ORE_SALT);
        var ore = weightedChoice(oreChoice, instance.ores(), OreEntry::weight);
        return Optional.of(ore.block());
    }

    public static BoundingBox bounds(OreVeinInstance instance) {
        return bounds(instance.shape(), instance.center());
    }

    public static Optional<BoundingBox> intersectingBounds(OreVeinInstance instance, BoundingBox generationBox) {
        return intersectingBounds(instance.shape(), instance.center(), generationBox);
    }

    private static <D> OreShapeInstance<?> sampleShape(OreShapeDefinition<D> definition, long veinSeed) {
        return sampleShape(definition, definition.shape(), veinSeed);
    }

    private static <D, I> OreShapeInstance<I> sampleShape(OreShapeDefinition<D> definition,
        IOreShape<D, I> shape, long veinSeed) {
        return new OreShapeInstance<>(definition.loc(), shape, shape.sample(definition.definition(), veinSeed));
    }

    private static <I> double fillFactor(OreShapeInstance<I> instance, long veinSeed, BlockPos center,
        BlockPos position) {
        return instance.shape().fillFactor(veinSeed, center, position, instance.instance());
    }

    private static <I> BoundingBox bounds(OreShapeInstance<I> instance, BlockPos center) {
        return instance.shape().bounds(center, instance.instance());
    }

    private static <I> Optional<BoundingBox> intersectingBounds(OreShapeInstance<I> instance, BlockPos center,
        BoundingBox generationBox) {
        return instance.shape().intersectingBounds(center, instance.instance(), generationBox);
    }

    private static <T> T weightedChoice(double unit, List<T> entries, ToDoubleFunction<T> weightFunc) {
        var totalWeight = entries.stream().mapToDouble(weightFunc).sum();
        var target = unit * totalWeight;
        var cumulative = 0d;
        for (var entry : entries) {
            cumulative += weightFunc.applyAsDouble(entry);
            if (target < cumulative) {
                return entry;
            }
        }
        return entries.getLast();
    }

    public static double hashToUnit(long seed, long salt) {
        return hashToUnit(seed, 0, 0, 0, salt);
    }

    public static double hashToUnit(long seed, BlockPos position, long salt) {
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

    @SuppressWarnings("unchecked")
    private static MapCodec<? extends OreShapeDefinition<?>> definitionCodecFor(IEntry<IOreShape<?, ?>> shape) {
        var loc = shape.loc();
        var typedShape = (IOreShape<Object, Object>) shape.get();
        return typedShape.definitionCodec().xmap(
            definition -> new OreShapeDefinition<>(loc, typedShape, definition),
            OreShapeDefinition::definition);
    }

    @SuppressWarnings("unchecked")
    private static MapCodec<? extends OreShapeInstance<?>> instanceCodecFor(IEntry<IOreShape<?, ?>> shape) {
        var loc = shape.loc();
        var typedShape = (IOreShape<Object, Object>) shape.get();
        return typedShape.instanceCodec().xmap(
            instance -> new OreShapeInstance<>(loc, typedShape, instance),
            OreShapeInstance::instance);
    }
}
