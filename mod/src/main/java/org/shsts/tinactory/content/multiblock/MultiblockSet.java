package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.integration.multiblock.BlockIngredient;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record MultiblockSet(List<IRecipeType<?>> types, IEntry<? extends Block> block,
    List<BlockIngredient> structureIngredients) {
    public MultiblockSet {
        types = List.copyOf(types);
        structureIngredients = List.copyOf(structureIngredients);
    }
}
