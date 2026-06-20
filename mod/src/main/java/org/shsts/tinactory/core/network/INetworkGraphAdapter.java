package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.api.network.ISubnetLabel;

import java.util.Collection;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INetworkGraphAdapter<TNodeData> {
    boolean isNodeLoaded(BlockPos pos);

    TNodeData getNodeData(BlockPos pos);

    boolean isConnected(BlockPos pos, TNodeData data, Direction dir);

    Collection<ISubnetLabel> allSubnetLabels();

    Collection<ISubnetLabel> subnetLabels(BlockPos pos, TNodeData data);

    void onDiscover(BlockPos pos, TNodeData data, Function<ISubnetLabel, BlockPos> subnets);

    void onConnectFinished();

    void onDisconnect(boolean connected);
}
