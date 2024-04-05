package org.shsts.tinactory.core.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IConnector {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir);

    boolean allowConnectFrom(Level world, BlockPos pos, BlockState state, Direction dir, BlockState state1);

    default boolean allowAutoConnectFrom(Level world, BlockPos pos, BlockState state,
                                         Direction dir, BlockState state1) {
        return allowConnectFrom(world, pos, state, dir, state1);
    }

    static boolean isConnectedInWorld(Level world, BlockPos pos, BlockState state, Direction dir) {
        return state.getBlock() instanceof IConnector connector &&
                connector.isConnected(world, pos, state, dir);
    }

    static boolean allowConnect(Level world, BlockPos pos, BlockState state, Direction dir) {
        var pos1 = pos.relative(dir);
        if (!world.isLoaded(pos1)) {
            return false;
        }
        var state1 = world.getBlockState(pos1);
        return state1.getBlock() instanceof IConnector connector &&
                connector.allowConnectFrom(world, pos1, state1, dir.getOpposite(), state);
    }

    static boolean autoConnectOnPlace(BlockPlaceContext ctx, BlockState state) {
        var world = ctx.getLevel();
        var dir = ctx.getClickedFace().getOpposite();
        var pos1 = ctx.getClickedPos().relative(dir);
        if (!world.isLoaded(pos1)) {
            return false;
        }
        var state1 = world.getBlockState(pos1);
        return state1.getBlock() instanceof IConnector connector &&
                connector.allowAutoConnectFrom(world, pos1, state1, dir.getOpposite(), state);
    }

    static boolean autoConnectFromNeighbor(Level world, BlockPos pos, BlockState state,
                                           Direction dir, BlockState state1) {
        return state.getBlock() instanceof IConnector connector &&
                connector.allowAutoConnectFrom(world, pos, state, dir, state1) &&
                connector.isConnected(world, pos, state, dir);
    }
}
