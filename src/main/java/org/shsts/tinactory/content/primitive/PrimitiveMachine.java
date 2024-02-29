package org.shsts.tinactory.content.primitive;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.SmartBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PrimitiveMachine extends SmartBlockEntity {
    public PrimitiveMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        var workSpeed = TinactoryConfig.INSTANCE.primitiveWorkSpeed.get();
        this.getCapability(AllCapabilities.PROCESSOR.get()).ifPresent(container -> {
            container.onPreWork();
            container.onWorkTick(workSpeed);
        });
    }
}
