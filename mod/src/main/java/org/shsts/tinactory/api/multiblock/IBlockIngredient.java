package org.shsts.tinactory.api.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IBlockIngredient extends Predicate<BlockState> {
    List<Block> expand(HolderLookup.Provider provider);

    BlockState display(HolderLookup.Provider provider);

    interface Value extends Predicate<BlockState> {
        void expand(HolderLookup.Provider provider, Consumer<Block> consumer);
    }
}
