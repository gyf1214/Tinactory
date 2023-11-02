package org.shsts.tinactory.content.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.SmartBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PrimitiveMachine extends SmartBlockEntity {
    private static final double WORK_SPEED = 0.25;

    public PrimitiveMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        this.getCapability(AllCapabilities.PROCESSING_MACHINE.get())
                .ifPresent(container -> container.onWorkTick(WORK_SPEED));
    }
}
