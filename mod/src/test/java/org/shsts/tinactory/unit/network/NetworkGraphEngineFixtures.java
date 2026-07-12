package org.shsts.tinactory.unit.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.api.network.ISubnetLabel;
import org.shsts.tinactory.core.network.INetworkGraphAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class NetworkGraphEngineFixtures {
    static final ISubnetLabel LABEL_A = new TestSubnetLabel();
    static final ISubnetLabel LABEL_B = new TestSubnetLabel();

    private NetworkGraphEngineFixtures() {}

    private static final class TestSubnetLabel implements ISubnetLabel {
    }

    static final class Node {
        private final EnumSet<Direction> edges = EnumSet.noneOf(Direction.class);
        private final List<ISubnetLabel> subnetLabels;

        Node(List<ISubnetLabel> subnetLabels) {
            this.subnetLabels = List.copyOf(subnetLabels);
        }

        List<ISubnetLabel> subnetLabels() {
            return subnetLabels;
        }
    }

    static final class Graph {
        private final Set<BlockPos> loaded = new HashSet<>();
        private final Map<BlockPos, Node> nodes = new HashMap<>();

        Graph addNode(BlockPos pos, boolean subnet) {
            return addNode(pos, subnet ? List.of(LABEL_A) : List.of());
        }

        Graph addNode(BlockPos pos, ISubnetLabel... subnetLabels) {
            return addNode(pos, List.of(subnetLabels));
        }

        private Graph addNode(BlockPos pos, List<ISubnetLabel> subnetLabels) {
            loaded.add(pos);
            nodes.put(pos, new Node(subnetLabels));
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
        final Map<BlockPos, Map<ISubnetLabel, BlockPos>> subnets = new HashMap<>();
        int connectFinishedCalls;
        int disconnectCalls;
        boolean lastDisconnectWasConnected;

        BlockPos subnet(BlockPos pos, ISubnetLabel label) {
            return subnets.get(pos).get(label);
        }
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
        public Collection<ISubnetLabel> allSubnetLabels() {
            return List.of(LABEL_A, LABEL_B);
        }

        @Override
        public Collection<ISubnetLabel> subnetLabels(BlockPos pos, Node data) {
            return data.subnetLabels();
        }

        @Override
        public void onDiscover(BlockPos pos, Node data, Function<ISubnetLabel, BlockPos> subnets) {
            events.discovered.add(pos);
            var snapshot = new HashMap<ISubnetLabel, BlockPos>();
            for (var label : allSubnetLabels()) {
                snapshot.put(label, subnets.apply(label));
            }
            events.subnets.put(pos, snapshot);
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
