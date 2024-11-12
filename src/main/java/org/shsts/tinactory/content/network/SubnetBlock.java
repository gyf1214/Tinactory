package org.shsts.tinactory.content.network;

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
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;

import java.util.function.Function;

import static org.shsts.tinactory.content.network.MachineBlock.IO_FACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SubnetBlock extends Block implements IWrenchable, IConnector, IElectricBlock {
    public final Voltage voltage;
    private final Voltage subVoltage;
    private final double resistance;

    public SubnetBlock(Properties properties, Voltage voltage, Voltage subVoltage) {
        super(properties.strength(2f, 6f).requiresCorrectToolForDrops());
        this.voltage = voltage;
        this.subVoltage = subVoltage;
        this.resistance = Math.sqrt((double) voltage.value / 2d) *
            TinactoryConfig.INSTANCE.machineResistanceFactor.get();
    }

    public static Function<Properties, SubnetBlock> transformer(Voltage voltage) {
        var subVoltage = Voltage.fromRank(voltage.rank - 1);
        return prop -> new SubnetBlock(prop, voltage, subVoltage);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IO_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(IO_FACING, ctx.getHorizontalDirection().getOpposite());
    }

    private void setIOFacing(Level world, BlockPos pos, BlockState state, Direction dir) {
        var oldDir = state.getValue(IO_FACING);
        world.setBlockAndUpdate(pos, state.setValue(IO_FACING, dir));

        NetworkManager.tryGet(world).ifPresent(manager -> {
            manager.invalidatePosDir(pos, dir);
            manager.invalidatePosDir(pos, dir.getOpposite());
            manager.invalidatePosDir(pos, oldDir);
            manager.invalidatePosDir(pos, oldDir.getOpposite());
        });
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
    public boolean canWrenchWith(ItemStack item) {
        return item.is(AllTags.TOOL_WRENCH);
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool,
        Direction dir, boolean sneaky) {
        if (!sneaky) {
            setIOFacing(world, pos, state, dir);
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir) {
        var myDir = state.getValue(IO_FACING);
        return dir == myDir || dir.getOpposite() == myDir;
    }

    @Override
    public boolean allowConnectWith(Level world, BlockPos pos, BlockState state,
        Direction dir, BlockState state1) {
        var myDir = state.getValue(IO_FACING);
        if (dir == myDir) {
            return IElectricBlock.canVoltagesConnect(voltage.value, state1);
        } else if (dir.getOpposite() == myDir) {
            return IElectricBlock.canVoltagesConnect(subVoltage.value, state1);
        }
        return false;
    }

    @Override
    public boolean isSubnet(Level world, BlockPos pos, BlockState state) {
        return true;
    }

    private void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGet(world).ifPresent(manager -> {
            var myDir = state.getValue(IO_FACING);
            manager.invalidatePosDir(pos, myDir);
            manager.invalidatePosDir(pos, myDir.getOpposite());
        });
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
