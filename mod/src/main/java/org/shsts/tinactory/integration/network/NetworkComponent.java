package org.shsts.tinactory.integration.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class NetworkComponent implements INetworkComponent {
    public interface Factory<T extends INetworkComponent> {
        T create(ComponentType<T> type, INetwork network);
    }

    protected final ComponentType<?> type;
    protected final INetwork network;

    public NetworkComponent(ComponentType<?> type, INetwork network) {
        this.type = type;
        this.network = network;
    }

    protected BlockPos getMachineSubnet(IMachine machine) {
        return network.getSubnet(machine.blockEntity().getBlockPos());
    }

    @Override
    public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {}

    @Override
    public void onConnect() {}

    @Override
    public void onPostConnect() {}

    @Override
    public void onDisconnect() {}
}
