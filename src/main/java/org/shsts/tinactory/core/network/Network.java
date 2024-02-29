package org.shsts.tinactory.core.network;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.TinactoryConfig;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Network {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Level world;

    public final NetworkManager manager;
    public final BlockPos center;

    public enum State {
        CONNECTED,
        CONNECTING,
        CONFLICT,
        DESTROYED
    }

    protected State state;
    private int delayTicks;

    public static class Ref {
        @Nullable
        private Network network;

        public Ref(Network network) {
            this.network = network;
        }

        public Optional<Network> get() {
            return Optional.ofNullable(this.network);
        }

        public boolean valid() {
            return this.network != null;
        }
    }

    @Nullable
    protected Ref ref;

    protected class BFSContext {
        private final Queue<BlockPos> queue = new ArrayDeque<>();
        public final Map<BlockPos, BlockState> visited = new HashMap<>();

        public void reset() {
            queue.clear();
            visited.clear();

            if (world.isLoaded(center)) {
                queue.add(center);
                visited.put(center, world.getBlockState(center));
            }
        }

        private boolean connected(BlockPos pos, BlockState blockState, Direction dir) {
            return IConnector.isConnectedInWorld(world, pos, blockState, dir);
        }

        public Optional<BlockPos> next() {
            if (queue.isEmpty()) {
                return Optional.empty();
            }

            var pos = queue.remove();
            var blockState = visited.get(pos);
            assert blockState != null;
            for (var dir : Direction.values()) {
                var pos1 = pos.relative(dir);
                if (visited.containsKey(pos1) || !world.isLoaded(pos1) || !connected(pos, blockState, dir)) {
                    continue;
                }
                var blockState1 = world.getBlockState(pos1);
                if (connected(pos1, blockState1, dir.getOpposite())) {
                    queue.add(pos1);
                    visited.put(pos1, blockState1);
                }
            }

            return Optional.of(pos);
        }
    }

    protected final BFSContext bfsContext;

    public Network(Level world, BlockPos center) {
        this.world = world;
        this.manager = NetworkManager.getInstance(world);
        this.center = center;
        this.bfsContext = new BFSContext();
        this.reset();
        this.manager.registerNetwork(this);
    }

    protected void onDisconnect() {
        LOGGER.debug("network {}: disconnect", this);
    }

    protected void reset() {
        this.ref = new Ref(this);
        this.state = State.CONNECTING;
        this.bfsContext.reset();
        this.delayTicks = 0;
    }

    public void invalidate() {
        if (this.state == State.DESTROYED) {
            return;
        }
        this.onDisconnect();
        if (this.ref != null) {
            this.ref.network = null;
        }
        this.reset();
        LOGGER.debug("network {}: invalidated", this);
    }

    public void destroy() {
        this.onDisconnect();
        if (this.ref != null) {
            this.ref.network = null;
        }
        this.ref = null;
        this.state = State.DESTROYED;
        this.bfsContext.reset();
        this.manager.unregisterNetwork(this);
        LOGGER.debug("network {}: destroyed", this);
    }

    public Ref ref() {
        assert this.ref != null;
        return ref;
    }

    protected void connectFinish() {
        LOGGER.debug("network {}: connect finished", this);
        this.state = State.CONNECTED;
        this.bfsContext.reset();
        this.ticks = 0;
    }

    protected void connectConflict(BlockPos pos) {
        LOGGER.debug("network {}: conflict detected at {}:{}", this, this.world.dimension(), pos);
        this.state = State.CONFLICT;
        this.bfsContext.reset();
    }

    protected void putBlock(BlockPos pos, BlockState state) {
        LOGGER.debug("network {}: add block {} at {}:{}", this, state, this.world.dimension(), pos);
    }

    protected boolean connectNextBlock() {
        if (!this.manager.registerNetwork(this)) {
            this.connectConflict(this.center);
            return false;
        }
        var nextPos = this.bfsContext.next();
        if (nextPos.isEmpty()) {
            this.connectFinish();
            return false;
        }
        var pos = nextPos.get();
        if (pos != this.center) {
            if (this.manager.hasNetworkAtPos(pos)) {
                this.connectConflict(pos);
                return false;
            }
            this.manager.putNetworkAtPos(pos, this);
        }
        var state = this.bfsContext.visited.get(pos);
        this.putBlock(pos, state);
        return true;
    }

    protected void doConnect() {
        var connectDelay = TinactoryConfig.INSTANCE.networkConnectDelay.get();
        var maxConnects = TinactoryConfig.INSTANCE.networkMaxConnectsPerTick.get();
        if (this.delayTicks < connectDelay) {
            this.delayTicks++;
            return;
        }
        for (var i = 0; i < maxConnects; i++) {
            if (!connectNextBlock()) {
                return;
            }
        }
    }

    private int ticks;

    protected void doTick() {
        if (ticks++ % 40 == 0) {
            LOGGER.debug("network {}: tick", this);
        }
    }

    public void tick() {
        switch (this.state) {
            case CONNECTING -> doConnect();
            case CONNECTED -> doTick();
        }
    }
}
