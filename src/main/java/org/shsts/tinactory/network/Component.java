package org.shsts.tinactory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class Component {
    public interface Factory<T extends Component> {
        T create(ComponentType<T> type, CompositeNetwork network);
    }

    protected final ComponentType<?> type;
    protected final CompositeNetwork network;

    public Component(ComponentType<?> type, CompositeNetwork network) {
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

    public interface Ticker {
        void tick(Level world, Network network);
    }

    public abstract void buildSchedulings(BiConsumer<Supplier<Scheduling>, Ticker> cons);
}
