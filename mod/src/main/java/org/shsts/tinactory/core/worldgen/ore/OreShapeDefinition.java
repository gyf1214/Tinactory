package org.shsts.tinactory.core.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreShapeDefinition<D>(IOreShape<D, ?> shape, D definition) {
    public OreShapeDefinition<D> scaleArea(double areaScale) {
        return new OreShapeDefinition<>(shape, shape.scaleDefinition(definition, areaScale));
    }
}
