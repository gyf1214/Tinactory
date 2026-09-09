package org.shsts.tinactory.core.worldgen.ore.shape;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreShapeInstance<I>(IOreShape<?, I> shape, I instance) {
    public OreShapeInstance {
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(instance, "instance");
    }
}
