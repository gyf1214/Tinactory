package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INetworkComponent {
    /**
     * Called during network connect when adding a block to the network.
     */
    void putBlock(BlockPos pos, BlockState state, Function<ISubnetLabel, BlockPos> subnets);

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
