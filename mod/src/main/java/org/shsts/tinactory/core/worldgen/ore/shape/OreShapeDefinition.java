package org.shsts.tinactory.core.worldgen.ore.shape;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreShapeDefinition<D>(IOreShape<D, ?> shape, D definition) {
    public OreShapeDefinition {
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(definition, "definition");
    }
}
