package org.shsts.tinactory.core.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.network.IScheduling;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class NetworkComponent {
    public interface Factory<T extends NetworkComponent> {
        T create(ComponentType<T> type, Network network);
    }

    protected final ComponentType<?> type;
    protected final Network network;

    public NetworkComponent(ComponentType<?> type, Network network) {
        this.type = type;
        this.network = network;
    }

    /**
     * Called during network connect when adding a block to the network.
     */
    public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {}

    /**
     * Called when network connect is finished.
     */
    public void onConnect() {}

    /**
     * Called when network disconnects.
     */
    public void onDisconnect() {}

    @FunctionalInterface
    public interface Ticker {
        void tick(Level world, Network network);
    }

    @FunctionalInterface
    public interface SchedulingBuilder {
        void add(Supplier<IScheduling> scheduling, NetworkComponent.Ticker ticker);
    }

    public abstract void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons);
}
