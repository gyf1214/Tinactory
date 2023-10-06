package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.shsts.tinactory.network.IConnector;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MachineBlock<T extends Machine> extends SmartEntityBlock<T> implements IWrenchable, IConnector {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty IO_FACING = DirectionProperty.create("io_facing");

    public MachineBlock(Properties properties, Supplier<SmartBlockEntityType<T>> entityType) {
        super(properties, entityType);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IO_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(IO_FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean canWrenchWith(ItemStack item) {
        return true;
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool, Direction dir, boolean sneaky) {
        if (!sneaky && dir != state.getValue(FACING)) {
            world.setBlockAndUpdate(pos, state.setValue(IO_FACING, dir));
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir) {
        return dir == state.getValue(IO_FACING);
    }
}
