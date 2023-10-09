package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.shsts.tinactory.network.IConnector;
import org.shsts.tinactory.network.NetworkManager;

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

    protected void setIOFacing(Level world, BlockPos pos, BlockState state, Direction dir) {
        var oldDir = state.getValue(IO_FACING);
        world.setBlockAndUpdate(pos, state.setValue(IO_FACING, dir));

        NetworkManager.tryGetInstance(world).ifPresent(manager -> {
            manager.invalidatePosDir(pos, dir);
            manager.invalidatePosDir(pos, oldDir);
        });
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool, Direction dir, boolean sneaky) {
        if (!sneaky && dir != state.getValue(FACING)) {
            this.setIOFacing(world, pos, state, dir);
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir) {
        return dir == state.getValue(IO_FACING);
    }

    protected void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGetInstance(world).ifPresent(manager -> {
            manager.invalidatePos(pos);
            manager.invalidatePosDir(pos, state.getValue(IO_FACING));
        });
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, world, pos, explosion);
        this.onDestroy(world, pos, state);
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        super.destroy(world, pos, state);
        this.onDestroy((Level) world, pos, state);
    }
}
