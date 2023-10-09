package org.shsts.tinactory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IConnector {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir);
}
