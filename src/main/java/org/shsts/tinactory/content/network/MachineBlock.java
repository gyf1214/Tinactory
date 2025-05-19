package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.TinactoryConfig.listConfig;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MachineBlock extends SmartEntityBlock
    implements IWrenchable, IConnector, IElectricBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty IO_FACING = DirectionProperty.create("io_facing");
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public final Voltage voltage;

    public MachineBlock(Properties properties,
        Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu, Voltage voltage) {
        super(properties.strength(2f, 6f).requiresCorrectToolForDrops(), entityType, menu);
        this.voltage = voltage;
    }

    public static Factory<MachineBlock> factory(Voltage voltage) {
        return (properties, entityType, menu) ->
            new MachineBlock(properties, entityType, menu, voltage);
    }

    public static Factory<MachineBlock> sided(Voltage voltage) {
        return (properties, entityType, menu) ->
            new SidedMachineBlock(properties, entityType, menu, voltage);
    }

    public static Factory<MachineBlock> multiblockInterface(Voltage voltage) {
        return (properties, entityType, menu) ->
            new MultiblockInterfaceBlock(properties, entityType, menu, voltage);
    }

    @Override
    protected BlockState createDefaultBlockState() {
        return super.createDefaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(IO_FACING, Direction.SOUTH)
            .setValue(WORKING, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IO_FACING, WORKING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite())
            .setValue(IO_FACING, ctx.getHorizontalDirection())
            .setValue(WORKING, false);
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
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool,
        Direction dir, boolean sneaky) {
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
        return voltage == Voltage.PRIMITIVE ? 0 :
            listConfig(CONFIG.machineResistanceFactor, voltage.rank - 1);
    }

    @Override
    public boolean allowConnectWith(Level world, BlockPos pos, BlockState state,
        Direction dir, BlockState state1) {
        return dir == state.getValue(IO_FACING) &&
            IElectricBlock.canVoltagesConnect(voltage.value, state1);
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
