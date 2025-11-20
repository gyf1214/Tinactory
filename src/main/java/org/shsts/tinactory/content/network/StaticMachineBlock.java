package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StaticMachineBlock extends MachineBlock {
    public StaticMachineBlock(Properties properties, Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu) {
        super(properties, entityType, menu, Voltage.PRIMITIVE);
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
}
