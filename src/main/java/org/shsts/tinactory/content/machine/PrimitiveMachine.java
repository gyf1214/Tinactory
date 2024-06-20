package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.SmartBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.network.MachineBlock.WORKING;

/**
 * Machine that can run without a network.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveMachine extends SmartBlockEntity {
    public PrimitiveMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);
        var workSpeed = TinactoryConfig.INSTANCE.primitiveWorkSpeed.get();
        var processor = AllCapabilities.PROCESSOR.get(this);
        processor.onPreWork();
        processor.onWorkTick(workSpeed);
        var working = processor.getProgress() > 0d;
        if (state.getValue(WORKING) != working) {
            world.setBlock(pos, state.setValue(WORKING, working), 3);
        }
    }
}
