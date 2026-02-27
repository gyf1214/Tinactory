package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
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

    void onPostConnect();

    /**
     * Called when network disconnects.
     */
    void onDisconnect();

    void buildSchedulings(ISchedulingRegister builder);
}
