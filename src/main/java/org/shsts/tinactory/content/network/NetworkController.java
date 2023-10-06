package org.shsts.tinactory.content.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.machine.Machine;

public class NetworkController extends Machine {
    private static final int MAX_STEP_PER_TICK = 100;

    public NetworkController(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
