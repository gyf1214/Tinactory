package org.shsts.tinactory.api.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMultiblockDisplay {
    record RequiredIngredient(IBlockIngredient ingredient, long count) {}

    int width();

    int depth();

    int height();

    BlockPos controllerPosition();

    Optional<IBlockIngredient> getIngredient(int x, int y, int z);

    List<RequiredIngredient> getRequiredIngredients();

    List<Component> getDetailLines();
}
