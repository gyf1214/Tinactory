package org.shsts.tinactory.core.network;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.core.tech.TeamProfile;
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
public class NetworkBase {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Level world;
    private final NetworkManager manager;
    public final BlockPos center;
    public final TeamProfile team;

    public enum State {
        CONNECTED,
        CONNECTING,
        CONFLICT,
        DESTROYED
    }

    private State state;
    private int delayTicks;

    public static class Ref {
        @Nullable
        private NetworkBase network;

        public Ref(NetworkBase network) {
            this.network = network;
        }

        public Optional<NetworkBase> get() {
            return Optional.ofNullable(network);
        }
    }

    @Nullable
    private Ref ref;

    private class BFSContext {
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

    private final BFSContext bfsContext;

    protected NetworkBase(Level world, BlockPos center, TeamProfile team) {
        this.world = world;
        this.manager = NetworkManager.getInstance(world);
        this.center = center;
        this.team = team;
        this.bfsContext = new BFSContext();

        reset();
        manager.registerNetwork(this);
    }

    public State getState() {
        return state;
    }

    protected void onDisconnect() {
        LOGGER.debug("network {}: disconnect", this);
    }

    private void reset() {
        ref = null;
        state = State.CONNECTING;
        bfsContext.reset();
        delayTicks = 0;
    }

    public void invalidate() {
        if (state == State.DESTROYED) {
            return;
        }
        onDisconnect();
        if (ref != null) {
            ref.network = null;
        }
        reset();
        LOGGER.debug("network {}: invalidated", this);
    }

    public void destroy() {
        if (state == State.DESTROYED) {
            return;
        }
        onDisconnect();
        if (ref != null) {
            ref.network = null;
        }
        ref = null;
        state = State.DESTROYED;
        bfsContext.reset();
        manager.unregisterNetwork(this);
        LOGGER.debug("network {}: destroyed", this);
    }

    public Ref ref() {
        if (ref == null) {
            ref = new Ref(this);
        }
        return ref;
    }

    protected void connectFinish() {
        LOGGER.debug("network {}: connect finished", this);
        state = State.CONNECTED;
        bfsContext.reset();
    }

    protected void connectConflict(BlockPos pos) {
        LOGGER.debug("network {}: conflict detected at {}:{}", this, world.dimension(), pos);
        state = State.CONFLICT;
        bfsContext.reset();
    }

    protected void putBlock(BlockPos pos, BlockState state) {
        LOGGER.debug("network {}: add block {} at {}:{}", this, state, world.dimension(), pos);
    }

    private boolean connectNextBlock() {
        if (!manager.registerNetwork(this)) {
            connectConflict(center);
            return false;
        }
        var nextPos = bfsContext.next();
        if (nextPos.isEmpty()) {
            connectFinish();
            return false;
        }
        var pos = nextPos.get();
        if (pos != center) {
            if (manager.hasNetworkAtPos(pos)) {
                connectConflict(pos);
                return false;
            }
            manager.putNetworkAtPos(pos, this);
        }
        var state = bfsContext.visited.get(pos);
        putBlock(pos, state);
        return true;
    }

    private void doConnect() {
        var connectDelay = TinactoryConfig.INSTANCE.networkConnectDelay.get();
        var maxConnects = TinactoryConfig.INSTANCE.networkMaxConnectsPerTick.get();
        if (delayTicks < connectDelay) {
            delayTicks++;
            return;
        }
        for (var i = 0; i < maxConnects; i++) {
            if (!connectNextBlock()) {
                return;
            }
        }
    }

    protected void doTick() {}

    public void tick() {
        switch (state) {
            case CONNECTING -> doConnect();
            case CONNECTED -> doTick();
        }
    }
}
