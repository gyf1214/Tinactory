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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MachineBlock<T extends BlockEntity> extends SmartEntityBlock<T>
        implements IWrenchable, IConnector, IElectricBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty IO_FACING = DirectionProperty.create("io_facing");

    protected final Voltage voltage;
    protected final double resistance;

    public MachineBlock(Properties properties, Supplier<SmartBlockEntityType<T>> entityType, Voltage voltage) {
        super(properties.strength(2f, 6f).requiresCorrectToolForDrops(), entityType);
        this.voltage = voltage;
        this.resistance = Math.sqrt((double) voltage.value / 2d) *
                TinactoryConfig.INSTANCE.machineResistanceFactor.get();
    }

    public static <T extends SmartBlockEntity>
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

        NetworkManager.tryGet(world).ifPresent(manager -> {
            manager.invalidatePosDir(pos, dir);
            manager.invalidatePosDir(pos, oldDir);
        });
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool, Direction dir, boolean sneaky) {
        if (!sneaky && dir != state.getValue(FACING)) {
            setIOFacing(world, pos, state, dir);
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir) {
        return dir == state.getValue(IO_FACING);
    }

    @Override
    public long getVoltage(BlockState state) {
        return voltage.value;
    }

    @Override
    public double getResistance(BlockState state) {
        return resistance;
    }

    @Override
    public boolean allowConnectFrom(Level world, BlockPos pos, BlockState state,
                                    Direction dir, BlockState state1) {
        return dir == state.getValue(IO_FACING) && state1.getBlock() instanceof IElectricBlock block1 &&
                (voltage == Voltage.PRIMITIVE || voltage.value == block1.getVoltage(state1));
    }

    protected void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGet(world).ifPresent(manager ->
                manager.invalidatePosDir(pos, state.getValue(IO_FACING)));
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, world, pos, explosion);
        onDestroy(world, pos, state);
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        super.destroy(world, pos, state);
        onDestroy((Level) world, pos, state);
    }
}
