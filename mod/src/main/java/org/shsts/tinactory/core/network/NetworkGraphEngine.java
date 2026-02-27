package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkGraphEngine<TNodeData> {
    public enum State {
        CONNECTED,
        CONNECTING,
        INVALIDATING
    }

    @FunctionalInterface
    public interface NodeConnected<TNodeData> {
        boolean isConnected(BlockPos pos, TNodeData data, Direction dir);
    }

    @FunctionalInterface
    public interface IsSubnet<TNodeData> {
        boolean isSubnet(BlockPos pos, TNodeData data);
    }

    @FunctionalInterface
    public interface OnDiscover<TNodeData> {
        void onDiscover(BlockPos pos, TNodeData data, BlockPos subnet);
    }

    public record BlockInfo<TNodeData>(TNodeData data, BlockPos parent, BlockPos subnet) {}

    private final BlockPos center;
    private final Predicate<BlockPos> isNodeLoaded;
    private final Function<BlockPos, TNodeData> getNodeData;
    private final NodeConnected<TNodeData> isConnected;
    private final IsSubnet<TNodeData> isSubnet;
    private final UUID priority;
    private final OnDiscover<TNodeData> onDiscover;
    private final Runnable onConnectFinished;
    private final java.util.function.Consumer<Boolean> onDisconnect;

    private final Queue<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, BlockInfo<TNodeData>> visited = new HashMap<>();
    private State state;

    public NetworkGraphEngine(UUID priority, BlockPos center, Predicate<BlockPos> isNodeLoaded,
        Function<BlockPos, TNodeData> getNodeData, NodeConnected<TNodeData> isConnected,
        IsSubnet<TNodeData> isSubnet,
        OnDiscover<TNodeData> onDiscover, Runnable onConnectFinished,
        java.util.function.Consumer<Boolean> onDisconnect) {
        this.priority = priority;
        this.center = center;
        this.isNodeLoaded = isNodeLoaded;
        this.getNodeData = getNodeData;
        this.isConnected = isConnected;
        this.isSubnet = isSubnet;
        this.onDiscover = onDiscover;
        this.onConnectFinished = onConnectFinished;
        this.onDisconnect = onDisconnect;
        reset();
    }

    public State state() {
        return state;
    }

    public boolean comparePriority(NetworkGraphEngine<TNodeData> another) {
        return priority.compareTo(another.priority) < 0;
    }

    public Optional<BlockInfo<TNodeData>> infoAt(BlockPos pos) {
        return Optional.ofNullable(visited.get(pos));
    }

    public void reset() {
        state = State.CONNECTING;
        queue.clear();
        visited.clear();
        if (isNodeLoaded.test(center)) {
            var data = getNodeData.apply(center);
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
        onDisconnect.accept(wasConnected);
        reset();
    }

    public boolean connectNext() {
        if (state != State.CONNECTING) {
            return false;
        }
        if (queue.isEmpty()) {
            state = State.CONNECTED;
            onConnectFinished.run();
            return false;
        }
        var pos = queue.remove();
        var info = visited.get(pos);
        assert info != null;
        var subnet = isSubnet.isSubnet(pos, info.data()) ? pos : info.subnet();
        for (var dir : Direction.values()) {
            var pos1 = pos.relative(dir);
            if (!isConnected.isConnected(pos, info.data(), dir)) {
                continue;
            }
            if (info.parent().equals(pos1)) {
                continue;
            }
            if (visited.containsKey(pos1) || !isNodeLoaded.test(pos1)) {
                continue;
            }
            var data1 = getNodeData.apply(pos1);
            if (isConnected.isConnected(pos1, data1, dir.getOpposite())) {
                queue.add(pos1);
                visited.put(pos1, new BlockInfo<>(data1, pos, subnet));
            }
        }
        onDiscover.onDiscover(pos, info.data(), subnet);
        return true;
    }
}
