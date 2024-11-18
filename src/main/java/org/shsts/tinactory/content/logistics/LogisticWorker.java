package org.shsts.tinactory.content.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.common.SmartBlockEntity;

public class LogisticWorker extends SmartBlockEntity {
    public LogisticWorker(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
