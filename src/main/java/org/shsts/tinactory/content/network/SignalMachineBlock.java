package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.shsts.tinactory.content.machine.ISignalMachine;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllCapabilities.SIGNAL_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SignalMachineBlock extends MachineBlock {
    public SignalMachineBlock(Properties properties,
        Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu, Voltage voltage) {
        super(properties, entityType, menu, voltage);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IO_FACING);
    }

    @Override
    protected BlockState createDefaultBlockState() {
        return stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(IO_FACING, Direction.SOUTH);
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
        return state.getValue(FACING) == dir;
    }
}
