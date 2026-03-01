package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INetworkGraphAdapter<TNodeData> {
    boolean isNodeLoaded(BlockPos pos);

    TNodeData getNodeData(BlockPos pos);

    boolean isConnected(BlockPos pos, TNodeData data, Direction dir);

    boolean isSubnet(BlockPos pos, TNodeData data);

    void onDiscover(BlockPos pos, TNodeData data, BlockPos subnet);

    void onConnectFinished();

    void onDisconnect(boolean connected);
}
