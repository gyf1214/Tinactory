package org.shsts.tinactory.core.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreShapeInstance<I>(IOreShape<?, I> shape, I instance) {}
