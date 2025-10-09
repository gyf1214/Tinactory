package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SidedMachineBlock extends MachineBlock {
    public SidedMachineBlock(Properties properties,
        Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu, Voltage voltage) {
        super(properties, entityType, menu, voltage);
    }

    @Override
    protected BlockState createDefaultBlockState() {
        return stateDefinition.any().setValue(IO_FACING, Direction.NORTH);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IO_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(IO_FACING, ctx.getHorizontalDirection());
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool,
        Direction dir, boolean sneaky) {
        if (!sneaky) {
            setIOFacing(world, pos, state, dir);
        }
    }
}
