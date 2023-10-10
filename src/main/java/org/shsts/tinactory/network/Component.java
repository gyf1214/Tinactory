package org.shsts.tinactory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class Component {
    public interface Factory<T extends Component> {
        T create(ComponentType<T> type, Network network);
    }

    protected final ComponentType<?> type;
    protected final Network network;

    public Component(ComponentType<?> type, Network network) {
        this.type = type;
        this.network = network;
    }

    /**
     * Called during network connect when adding a block to the network.
     */
    public void putBlock(BlockPos pos, BlockState state) {}

    /**
     * Called when network connect is finished.
     */
    public void onConnect() {}

    /**
     * Called when network disconnects.
     */
    public void onDisconnect() {}
}
