package org.shsts.tinactory.content.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IWrenchable {
    boolean canWrenchWith(ItemStack item);

    void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool, Direction dir, boolean sneaky);
}
