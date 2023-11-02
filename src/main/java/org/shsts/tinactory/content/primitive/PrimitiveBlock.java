package org.shsts.tinactory.content.primitive;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.core.SmartEntityBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class PrimitiveBlock<T extends BlockEntity> extends SmartEntityBlock<T> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PrimitiveBlock(Properties properties, Supplier<SmartBlockEntityType<T>> entityType) {
        super(properties, entityType);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
