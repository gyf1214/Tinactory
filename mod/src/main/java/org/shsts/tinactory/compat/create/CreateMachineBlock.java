package org.shsts.tinactory.compat.create;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.integration.common.SmartEntityBlock;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateMachineBlock extends MachineBlock implements IRotate {
    private static final SmartBlockEntityTicker<KineticBlockEntity> KINETIC_TICKER = new SmartBlockEntityTicker<>();

    public CreateMachineBlock(Properties properties, Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu, Voltage voltage) {
        super(properties, entityType, menu, voltage);
    }

    public static SmartEntityBlock.Factory<CreateMachineBlock> factory(Voltage voltage) {
        return (properties, entityType, menu) -> new CreateMachineBlock(properties, entityType, menu, voltage);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction direction) {
        return state.getValue(FACING) == direction;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type) {
        var containerTicker = super.getTicker(level, state, type);
        if (containerTicker == null) {
            return null;
        }
        return (world, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof KineticBlockEntity kinetic) {
                KINETIC_TICKER.tick(world, pos, blockState, kinetic);
            }
            containerTicker.tick(world, pos, blockState, blockEntity);
        };
    }
}
