package org.shsts.tinactory.content.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.core.network.IConnector;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IElectricConnector extends IConnector, IElectricBlock {
    boolean allowConnectFrom(Level world, BlockPos pos, BlockState state, Direction dir, BlockState state1);

    boolean allowAutoConnectFrom(Level world, BlockPos pos, BlockState state, Direction dir, BlockState state1);

    static boolean allowConnect(Level world, BlockPos pos, BlockState state, Direction dir) {
        var pos1 = pos.relative(dir);
        if (!world.isLoaded(pos1)) {
            return false;
        }
        var state1 = world.getBlockState(pos1);
        return state1.getBlock() instanceof IElectricConnector connector &&
                connector.allowConnectFrom(world, pos1, state1, dir.getOpposite(), state);
    }

    static boolean allowAutoConnect(Level world, BlockPos pos, BlockState state, Direction dir) {
        var pos1 = pos.relative(dir);
        if (!world.isLoaded(pos1)) {
            return false;
        }
        var state1 = world.getBlockState(pos1);
        return state1.getBlock() instanceof IElectricConnector connector &&
                connector.allowAutoConnectFrom(world, pos1, state1, dir.getOpposite(), state);
    }

    static boolean allowAutoConnect(BlockPlaceContext ctx, BlockState state) {
        return allowAutoConnect(ctx.getLevel(), ctx.getClickedPos(), state,
                ctx.getClickedFace().getOpposite());
    }
}
