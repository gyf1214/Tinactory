package org.shsts.tinactory.core.multiblock;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.network.SidedMachineBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;

import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiBlockInterfaceBlock extends SidedMachineBlock<SmartBlockEntity> {
    public static final BooleanProperty JOINED = BooleanProperty.create("joined");

    public MultiBlockInterfaceBlock(Properties properties,
        Supplier<SmartBlockEntityType<SmartBlockEntity>> entityType,
        Voltage voltage) {
        super(properties, entityType, voltage);
    }

    @Override
    protected BlockState createDefaultBlockState() {
        return super.createDefaultBlockState().setValue(JOINED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(JOINED);
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(JOINED, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(JOINED) ?
            RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    public static int tint(Voltage v, BlockState state, int index) {
        var target = state.getValue(JOINED) ? 2 : 0;
        return index == target ? v.color : 0xFFFFFFFF;
    }
}
