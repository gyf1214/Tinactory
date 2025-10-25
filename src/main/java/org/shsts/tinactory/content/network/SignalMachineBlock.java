package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.logistics.ISignalMachine;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllCapabilities.SIGNAL_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SignalMachineBlock extends StaticMachineBlock {
    public SignalMachineBlock(Properties properties,
        Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu, Voltage voltage) {
        super(properties, entityType, menu, voltage);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction dir) {
        if (!(world instanceof Level world1) || dir != state.getValue(FACING).getOpposite()) {
            return 0;
        }
        return getBlockEntity(world1, pos)
            .flatMap(SIGNAL_MACHINE::tryGet)
            .map(ISignalMachine::getSignal)
            .orElse(0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction dir) {
        return state.getSignal(world, pos, dir);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos,
        @Nullable Direction dir) {
        return state.getValue(FACING).getOpposite() == dir;
    }

    public static void updateSignal(Level world, BlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        var state = blockEntity.getBlockState();
        var dir = state.getValue(FACING);
        world.neighborChanged(pos.relative(dir), state.getBlock(), pos);
    }
}
