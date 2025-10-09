package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.shsts.tinactory.content.network.MachineBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TurbineBlock extends Block {
    public static final int BLADES = 9;
    public static final int CENTER_BLADE = 4;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WORKING = MachineBlock.WORKING;
    public static final IntegerProperty BLADE = IntegerProperty.create("blade", 0, BLADES - 1);

    public TurbineBlock(Properties properties) {
        super(properties);
        var defaultState = stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(WORKING, false)
            .setValue(BLADE, CENTER_BLADE);
        registerDefaultState(defaultState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING, BLADE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
