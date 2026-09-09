package org.shsts.tinactory.core.worldgen.ore.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreShapeUtil {
    private OreShapeUtil() {}

    public static MapCodec<OreShapeDefinition<?>> definitionCodec(Codec<IOreShape<?, ?>> shapeCodec) {
        Objects.requireNonNull(shapeCodec, "shapeCodec");
        return shapeCodec.dispatchMap(OreShapeDefinition::shape, OreShapeUtil::definitionCodecFor);
    }

    public static MapCodec<OreShapeInstance<?>> instanceCodec(Codec<IOreShape<?, ?>> shapeCodec) {
        Objects.requireNonNull(shapeCodec, "shapeCodec");
        return shapeCodec.dispatchMap(OreShapeInstance::shape, OreShapeUtil::instanceCodecFor);
    }

    @SuppressWarnings("unchecked")
    private static MapCodec<? extends OreShapeDefinition<?>> definitionCodecFor(IOreShape<?, ?> shape) {
        var typedShape = (IOreShape<Object, Object>) shape;
        return typedShape.definitionCodec().xmap(
            definition -> new OreShapeDefinition<>(typedShape, definition),
            value -> {
                if (value.shape() != shape) {
                    throw new IllegalArgumentException("shape definition belongs to a different shape");
                }
                return value.definition();
            });
    }

    @SuppressWarnings("unchecked")
    private static MapCodec<? extends OreShapeInstance<?>> instanceCodecFor(IOreShape<?, ?> shape) {
        var typedShape = (IOreShape<Object, Object>) shape;
        return typedShape.instanceCodec().xmap(
            instance -> new OreShapeInstance<>(typedShape, instance),
            value -> {
                if (value.shape() != shape) {
                    throw new IllegalArgumentException("shape instance belongs to a different shape");
                }
                return value.instance();
            });
    }
}
