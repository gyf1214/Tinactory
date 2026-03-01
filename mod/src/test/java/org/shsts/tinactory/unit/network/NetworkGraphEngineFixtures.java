package org.shsts.tinactory.unit.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.core.network.INetworkGraphAdapter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class NetworkGraphEngineFixtures {
    private NetworkGraphEngineFixtures() {}

    static final class Node {
        private final EnumSet<Direction> edges = EnumSet.noneOf(Direction.class);
        private final boolean subnet;

        Node(boolean subnet) {
            this.subnet = subnet;
        }

        boolean isSubnet() {
            return subnet;
        }
    }

    static final class Graph {
        private final Set<BlockPos> loaded = new HashSet<>();
        private final Map<BlockPos, Node> nodes = new HashMap<>();

        Graph addNode(BlockPos pos, boolean subnet) {
            loaded.add(pos);
            nodes.put(pos, new Node(subnet));
            return this;
        }

        Graph addEdge(BlockPos from, Direction dir) {
            var to = from.relative(dir);
            nodes.get(from).edges.add(dir);
            nodes.get(to).edges.add(dir.getOpposite());
            return this;
        }

        boolean isLoaded(BlockPos pos) {
            return loaded.contains(pos);
        }

        Node node(BlockPos pos) {
            return nodes.get(pos);
        }

        boolean connected(BlockPos pos, Direction dir) {
            var node = nodes.get(pos);
            return node != null && node.edges.contains(dir);
        }
    }

    static final class Events {
        final List<BlockPos> discovered = new ArrayList<>();
        final Map<BlockPos, BlockPos> subnets = new HashMap<>();
        int connectFinishedCalls;
        int disconnectCalls;
        boolean lastDisconnectWasConnected;
    }

    static final class RecordingAdapter implements INetworkGraphAdapter<Node> {
        private final Graph graph;
        private final Events events;

        RecordingAdapter(Graph graph, Events events) {
            this.graph = graph;
            this.events = events;
        }

        @Override
        public boolean isNodeLoaded(BlockPos pos) {
            return graph.isLoaded(pos);
        }

        @Override
        public Node getNodeData(BlockPos pos) {
            return graph.node(pos);
        }

        @Override
        public boolean isConnected(BlockPos pos, Node data, Direction dir) {
            return graph.connected(pos, dir);
        }

        @Override
        public boolean isSubnet(BlockPos pos, Node data) {
            return data.isSubnet();
        }

        @Override
        public void onDiscover(BlockPos pos, Node data, BlockPos subnet) {
            events.discovered.add(pos);
            events.subnets.put(pos, subnet);
        }

        @Override
        public void onConnectFinished() {
            events.connectFinishedCalls++;
        }

        @Override
        public void onDisconnect(boolean connected) {
            events.disconnectCalls++;
            events.lastDisconnectWasConnected = connected;
        }
    }
}
