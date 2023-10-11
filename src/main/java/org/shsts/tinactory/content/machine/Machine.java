package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.network.CompositeNetwork;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends SmartBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected CompositeNetwork network;

    public Machine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onLoad(Level world) {
        LOGGER.debug("machine {}: loaded", this);
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        LOGGER.debug("machine {}: removed in world", this);
        if (this.network != null) {
            this.network.invalidate();
        }
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        LOGGER.debug("machine {}: removed by chunk unload", this);
        if (this.network != null) {
            this.network.invalidate();
        }
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(CompositeNetwork network) {
        LOGGER.debug("machine {}: connect to network {}", this, network);
        this.network = network;
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        LOGGER.debug("machine {}: disconnect from network", this);
        this.network = null;
    }
}
