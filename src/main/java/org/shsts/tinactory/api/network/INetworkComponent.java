package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INetworkComponent {
    /**
     * Called during network connect when adding a block to the network.
     */
    void putBlock(BlockPos pos, BlockState state, BlockPos subnet);

    /**
     * Called when network connect is finished.
     */
    void onConnect();

    /**
     * Called when network disconnects.
     */
    void onDisconnect();

    void buildSchedulings(SchedulingBuilder builder);

    @FunctionalInterface
    interface Ticker {
        void tick(Level world, INetwork network);
    }

    @FunctionalInterface
    interface SchedulingBuilder {
        void add(IScheduling scheduling, Ticker ticker);
    }
}
