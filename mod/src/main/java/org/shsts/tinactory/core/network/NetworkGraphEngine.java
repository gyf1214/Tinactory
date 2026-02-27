package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkGraphEngine<TNodeData> {
    public enum State {
        CONNECTED,
        CONNECTING,
        INVALIDATING
    }

    private record BlockInfo<TNodeData>(TNodeData data, BlockPos parent, BlockPos subnet) {}

    private final BlockPos center;
    private final NetworkManager manager;
    private final INetworkGraphAdapter<TNodeData> adapter;
    private final UUID priority;

    private final Queue<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, BlockInfo<TNodeData>> visited = new HashMap<>();
    private State state;

    public NetworkGraphEngine(UUID priority, BlockPos center, NetworkManager manager,
        INetworkGraphAdapter<TNodeData> adapter) {
        this.priority = priority;
        this.center = center;
        this.manager = manager;
        this.adapter = adapter;
        reset();
    }

    public State state() {
        return state;
    }

    public boolean comparePriority(NetworkGraphEngine<?> another) {
        return priority.compareTo(another.priority) < 0;
    }

    public void reset() {
        state = State.CONNECTING;
        queue.clear();
        visited.clear();
        if (adapter.isNodeLoaded(center)) {
            var data = adapter.getNodeData(center);
            queue.add(center);
            visited.put(center, new BlockInfo<>(data, center, center));
        }
    }

    public void invalidate() {
        if (state == State.INVALIDATING) {
            return;
        }
        var wasConnected = state == State.CONNECTED;
        state = State.INVALIDATING;
        adapter.onDisconnect(wasConnected);
        reset();
    }

    public boolean connectNext() {
        if (state != State.CONNECTING) {
            return false;
        }
        if (queue.isEmpty()) {
            state = State.CONNECTED;
            adapter.onConnectFinished();
            return false;
        }
        var pos = queue.remove();
        var info = visited.get(pos);
        assert info != null;
        var subnet = adapter.isSubnet(pos, info.data()) ? pos : info.subnet();
        for (var dir : Direction.values()) {
            var pos1 = pos.relative(dir);
            if (!adapter.isConnected(pos, info.data(), dir)) {
                continue;
            }
            if (info.parent().equals(pos1)) {
                continue;
            }
            if (visited.containsKey(pos1) || !adapter.isNodeLoaded(pos1)) {
                continue;
            }
            var data1 = adapter.getNodeData(pos1);
            if (adapter.isConnected(pos1, data1, dir.getOpposite())) {
                queue.add(pos1);
                visited.put(pos1, new BlockInfo<>(data1, pos, subnet));
            }
        }
        if (manager.hasNetworkAtPos(pos)) {
            var network1 = manager.getNetworkAtPos(pos).orElseThrow();
            if (network1 != this) {
                if (comparePriority(network1)) {
                    network1.invalidate();
                } else {
                    invalidate();
                    return false;
                }
            }
        }
        if (!manager.hasNetworkAtPos(pos)) {
            manager.putNetworkAtPos(pos, this);
        }
        adapter.onDiscover(pos, info.data(), subnet);
        return true;
    }
}
