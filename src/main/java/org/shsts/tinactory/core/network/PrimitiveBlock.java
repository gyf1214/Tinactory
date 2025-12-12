package org.shsts.tinactory.core.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

import static org.shsts.tinactory.core.network.MachineBlock.WORKING;

/**
 * Entity block that has a face and do not connect to a network.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveBlock extends SmartEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PrimitiveBlock(Properties properties,
        Supplier<IBlockEntityType> entityType, @Nullable IMenuType menu) {
        super(properties.isValidSpawn(AllItems::never), entityType, menu);
    }

    @Override
    protected BlockState createDefaultBlockState() {
        return super.createDefaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(WORKING, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
