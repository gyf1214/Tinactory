package org.shsts.tinactory.api.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMultiblockDisplay {
    int width();

    int depth();

    int height();

    BlockPos controllerPosition();

    Optional<IBlockIngredient> getIngredient(int x, int y, int z);
}
