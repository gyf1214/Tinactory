package org.shsts.tinactory.core.network;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.common.WeakMap;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;

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
        INVALIDATING
    }

    private State state;
    private int delayTicks;

    @Nullable
    private WeakMap.Ref<NetworkBase> ref = null;

    private class BFSContext {
        public record BlockInfo(BlockState state, BlockPos parent, BlockPos subnet) {}

        private final Queue<BlockPos> queue = new ArrayDeque<>();
        public final Map<BlockPos, BlockInfo> visited = new HashMap<>();

        public void reset() {
            queue.clear();
            visited.clear();

            if (world.isLoaded(center)) {
                queue.add(center);
                visited.put(center, new BlockInfo(world.getBlockState(center), center, center));
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
            var info = visited.get(pos);
            assert info != null;
            var subnet = IConnector.isSubnetInWorld(world, pos, info.state) ?
                pos : info.subnet;
            for (var dir : Direction.values()) {
                var pos1 = pos.relative(dir);

                if (!connected(pos, info.state, dir)) {
                    continue;
                }

                if (info.parent.equals(pos1)) {
                    continue;
                }

                if (visited.containsKey(pos1)) {
                    continue;
                }

                var blockState1 = world.getBlockState(pos1);
                if (connected(pos1, blockState1, dir.getOpposite())) {
                    queue.add(pos1);
                    visited.put(pos1, new BlockInfo(blockState1, pos, subnet));
                }
            }

            return Optional.of(pos);
        }
    }

    private final BFSContext bfsContext;

    protected NetworkBase(Level world, BlockPos center, TeamProfile team) {
        this.world = world;
        this.manager = NetworkManager.get(world);
        this.center = center;
        this.team = team;
        this.bfsContext = new BFSContext();

        reset();
    }

    public State getState() {
        return state;
    }

    protected void onDisconnect() {
        LOGGER.debug("{}: disconnect", this);
    }

    private void reset() {
        ref = null;
        state = State.CONNECTING;
        bfsContext.reset();
        delayTicks = 0;
    }

    public void invalidate() {
        if (state == State.INVALIDATING) {
            return;
        }
        var state0 = state;
        state = State.INVALIDATING;
        if (state0 == State.CONNECTED) {
            onDisconnect();
        }
        if (ref != null) {
            ref.invalidate();
        }
        reset();
        LOGGER.debug("{}: invalidated", this);
    }

    public <K> void addToMap(WeakMap<K, NetworkBase> map, K key) {
        if (ref == null) {
            ref = map.put(key, this);
        } else {
            map.put(key, ref);
        }
    }

    protected void connectFinish() {
        LOGGER.debug("{}: connect finished", this);
        state = State.CONNECTED;
        bfsContext.reset();
    }

    protected void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        LOGGER.trace("{}: add block {} at {}:{}, subnet = {}", this, state,
            world.dimension(), pos, subnet);
    }

    private boolean connectNextBlock() {
        var nextPos = bfsContext.next();
        if (nextPos.isEmpty()) {
            connectFinish();
            return false;
        }
        var pos = nextPos.get();
        if (manager.hasNetworkAtPos(pos)) {
            manager.invalidatePos(pos);
        }
        manager.putNetworkAtPos(pos, this);
        var info = bfsContext.visited.get(pos);
        putBlock(pos, info.state, info.subnet);
        return true;
    }

    private void doConnect() {
        var connectDelay = CONFIG.networkConnectDelay.get();
        var maxConnects = CONFIG.networkMaxConnectsPerTick.get();
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
