package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.network.CableBlock;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.core.tool.IWrenchable;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.TinactoryConfig.listConfig;
import static org.shsts.tinactory.core.network.MachineBlock.IO_FACING;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SubnetBlock extends Block implements IWrenchable, IConnector, IElectricBlock {
    public final Voltage voltage;
    private final Voltage subVoltage;

    public SubnetBlock(Properties properties, Voltage voltage, Voltage subVoltage) {
        super(properties.requiresCorrectToolForDrops().isValidSpawn(AllItems::never));
        this.voltage = voltage;
        this.subVoltage = subVoltage;

        registerDefaultState(stateDefinition.any().setValue(IO_FACING, Direction.NORTH));
    }

    public static Function<Properties, SubnetBlock> factory(Voltage voltage, Voltage subVoltage) {
        return prop -> new SubnetBlock(prop, voltage, subVoltage);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IO_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(IO_FACING, ctx.getHorizontalDirection().getOpposite());
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
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip,
        TooltipFlag isAdvanced) {
        if (voltage == subVoltage) {
            addTooltip(tooltip, "machineVoltage", NUMBER_FORMAT.format(voltage.value), voltage.displayName());
        } else {
            addTooltip(tooltip, "transformer.1", NUMBER_FORMAT.format(voltage.value), voltage.displayName());
            addTooltip(tooltip, "transformer.2", NUMBER_FORMAT.format(subVoltage.value), subVoltage.displayName());
        }
    }

    @Override
    public long getVoltage(BlockState state) {
        return voltage.value;
    }

    @Override
    public double getResistance(BlockState state) {
        return listConfig(CONFIG.machineResistanceFactor, voltage.rank - 1);
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
        if (!(state1.getBlock() instanceof CableBlock)) {
            return false;
        }
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
