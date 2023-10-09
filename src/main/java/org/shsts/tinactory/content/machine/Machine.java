package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends SmartBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Machine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onLoad(Level world) {
        LOGGER.debug("on load {}", this);
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        LOGGER.debug("on remove in world {}", this);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        LOGGER.debug("on remove by chunk unload {}", this);
    }

    private int ticks = 0;

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (this.ticks++ % 40 == 0) {
            LOGGER.debug("tick {}", this);
        }
    }
}
