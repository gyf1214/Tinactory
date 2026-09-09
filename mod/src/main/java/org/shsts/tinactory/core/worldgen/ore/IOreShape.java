package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IOreShape<D, I> {
    MapCodec<D> definitionCodec();

    MapCodec<I> instanceCodec();

    I sample(D definition, long veinSeed);

    BoundingBox bounds(BlockPos center, I instance);

    double fillFactor(long veinSeed, BlockPos center, BlockPos position, I instance);
}
