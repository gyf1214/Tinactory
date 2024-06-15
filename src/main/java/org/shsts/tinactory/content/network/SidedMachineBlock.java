package org.shsts.tinactory.content.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.common.SmartBlockEntityType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SidedMachineBlock<T extends BlockEntity> extends MachineBlock<T> {
    public SidedMachineBlock(Properties properties, Supplier<SmartBlockEntityType<T>> entityType, Voltage voltage) {
        super(properties, entityType, voltage);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IO_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(IO_FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool,
                             Direction dir, boolean sneaky) {
        if (!sneaky) {
            setIOFacing(world, pos, state, dir);
        }
    }
}
