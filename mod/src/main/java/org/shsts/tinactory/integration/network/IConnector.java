package org.shsts.tinactory.integration.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.network.ISubnetLabel;

import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IConnector {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir);

    boolean allowConnectWith(Level world, BlockPos pos, BlockState state, Direction dir, BlockState state1);

    default boolean autoConnectWith(Level world, BlockPos pos, BlockState state,
        Direction dir, BlockState state1) {
        return allowConnectWith(world, pos, state, dir, state1);
    }

    default boolean allowConnectWith(Level world, BlockPos pos, BlockState state, Direction dir) {
        var pos1 = pos.relative(dir);
        return world.isLoaded(pos1) &&
            allowConnectWith(world, pos, state, dir, world.getBlockState(pos1));
    }

    default boolean autoConnectWith(Level world, BlockPos pos, BlockState state, Direction dir) {
        var pos1 = pos.relative(dir);
        if (!world.isLoaded(pos1)) {
            return false;
        }
        return autoConnectWith(world, pos, state, dir, world.getBlockState(pos1));
    }

    default Collection<ISubnetLabel> subnetLabels(Level world, BlockPos pos, BlockState state) {
        return List.of();
    }

    static boolean allowConnectWith(Level world, BlockPos pos, Direction dir) {
        if (!world.isLoaded(pos)) {
            return false;
        }
        var state = world.getBlockState(pos);
        return state.getBlock() instanceof IConnector connector &&
            connector.allowConnectWith(world, pos, state, dir);
    }

    static boolean autoConnectWith(Level world, BlockPos pos, Direction dir, BlockState state1) {
        if (!world.isLoaded(pos)) {
            return false;
        }
        var state = world.getBlockState(pos);
        return state.getBlock() instanceof IConnector connector &&
            connector.autoConnectWith(world, pos, state, dir, state1);
    }

    static boolean isConnectedInWorld(Level world, BlockPos pos, BlockState state, Direction dir) {
        return state.getBlock() instanceof IConnector connector &&
            connector.isConnected(world, pos, state, dir) &&
            connector.allowConnectWith(world, pos, state, dir);
    }

    static Collection<ISubnetLabel> subnetLabelsInWorld(Level world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof IConnector connector ?
            connector.subnetLabels(world, pos, state) : List.of();
    }
}
