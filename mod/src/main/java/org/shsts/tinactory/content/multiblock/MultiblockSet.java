package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record MultiblockSet(List<IRecipeType<?>> types, IEntry<? extends Block> controller, IMultiblockDisplay display,
    List<IBlockIngredient> requiredIngredients) {
    public MultiblockSet {
        types = List.copyOf(types);
        requiredIngredients = List.copyOf(requiredIngredients);
    }
}
