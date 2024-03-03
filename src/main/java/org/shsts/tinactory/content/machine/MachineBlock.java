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
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MachineBlock<T extends Machine> extends SmartEntityBlock<T>
        implements IWrenchable, IConnector, IElectricBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty IO_FACING = DirectionProperty.create("io_facing");

    protected final Voltage voltage;
    protected final double resistance;

    public MachineBlock(Properties properties, Supplier<SmartBlockEntityType<T>> entityType, Voltage voltage) {
        super(properties.strength(2.0f, 6.0f).requiresCorrectToolForDrops(), entityType);
        this.voltage = voltage;
        this.resistance = Math.sqrt((double) voltage.val / 2d);
    }

    public static <T extends Machine>
    EntityBlockBuilder.Factory<T, MachineBlock<T>> factory(Voltage voltage) {
        return (properties, entityType) -> new MachineBlock<>(properties, entityType, voltage);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IO_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(IO_FACING, ctx.getHorizontalDirection());
    }

    @Override
    public boolean canWrenchWith(ItemStack item) {
        return item.is(AllTags.TOOL_WRENCH);
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

    @Override
    public long getVoltage(BlockState state) {
        return this.voltage.val;
    }

    @Override
    public double getResistance(BlockState state) {
        return this.resistance;
    }

    @Override
    public boolean allowConnectFrom(Level world, BlockPos pos, BlockState state,
                                    Direction dir, BlockState state1) {
        return dir == state.getValue(IO_FACING) && state1.getBlock() instanceof IElectricBlock block1 &&
                (this.voltage == Voltage.PRIMITIVE || this.voltage.val == block1.getVoltage(state1));
    }

    protected void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGetInstance(world).ifPresent(manager ->
                manager.invalidatePosDir(pos, state.getValue(IO_FACING)));
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
