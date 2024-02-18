package org.shsts.tinactory.core.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IConnector {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir);

    static boolean isConnectedInWorld(Level world, BlockPos pos, BlockState state, Direction dir) {
        return state.getBlock() instanceof IConnector connector &&
                connector.isConnected(world, pos, state, dir);
    }

    static boolean isConnectedInWorld(Level world, BlockPos pos, Direction dir) {
        return world.isLoaded(pos) && isConnectedInWorld(world, pos, world.getBlockState(pos), dir);
    }
}
