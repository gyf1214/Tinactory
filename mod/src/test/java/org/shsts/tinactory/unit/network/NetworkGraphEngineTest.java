package org.shsts.tinactory.unit.network;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.network.NetworkGraphEngine;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkGraphEngineTest {
    @Test
    void shouldDiscoverAllReachableNodesByBfs() {
        var center = new net.minecraft.core.BlockPos(0, 0, 0);
        var east = center.east();
        var west = center.west();
        var south = center.south();
        var graph = new NetworkGraphEngineFixtures.Graph()
            .addNode(center, false)
            .addNode(east, false)
            .addNode(west, false)
            .addNode(south, false)
            .addEdge(center, net.minecraft.core.Direction.EAST)
            .addEdge(center, net.minecraft.core.Direction.WEST)
            .addEdge(center, net.minecraft.core.Direction.SOUTH);
        var events = new NetworkGraphEngineFixtures.Events();

        var engine = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            graph::isLoaded,
            graph::node,
            (pos, node, dir) -> graph.connected(pos, dir),
            (pos, node) -> node.isSubnet(),
            (pos, node, subnet) -> {
                events.discovered.add(pos);
                events.subnets.put(pos, subnet);
            },
            () -> events.connectFinishedCalls++,
            connected -> {
                events.disconnectCalls++;
                events.lastDisconnectWasConnected = connected;
            }
        );

        while (engine.connectNext()) {
            // step until finished.
        }

        assertEquals(NetworkGraphEngine.State.CONNECTED, engine.state());
        assertEquals(1, events.connectFinishedCalls);
        assertEquals(Set.of(center, east, west, south), Set.copyOf(events.discovered));
    }

    @Test
    void shouldPropagateSubnetFromParentUntilSubnetMarker() {
        var center = new net.minecraft.core.BlockPos(0, 0, 0);
        var east = center.east();
        var eastEast = east.east();
        var graph = new NetworkGraphEngineFixtures.Graph()
            .addNode(center, false)
            .addNode(east, true)
            .addNode(eastEast, false)
            .addEdge(center, net.minecraft.core.Direction.EAST)
            .addEdge(east, net.minecraft.core.Direction.EAST);
        var events = new NetworkGraphEngineFixtures.Events();

        var engine = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            graph::isLoaded,
            graph::node,
            (pos, node, dir) -> graph.connected(pos, dir),
            (pos, node) -> node.isSubnet(),
            (pos, node, subnet) -> events.subnets.put(pos, subnet),
            () -> events.connectFinishedCalls++,
            connected -> {
                events.disconnectCalls++;
                events.lastDisconnectWasConnected = connected;
            }
        );

        while (engine.connectNext()) {
            // step until finished.
        }

        assertEquals(center, events.subnets.get(center));
        assertEquals(east, events.subnets.get(east));
        assertEquals(east, events.subnets.get(eastEast));
    }

    @Test
    void shouldResetTraversalAfterInvalidateAndReconnect() {
        var center = new net.minecraft.core.BlockPos(0, 0, 0);
        var east = center.east();
        var graph = new NetworkGraphEngineFixtures.Graph()
            .addNode(center, false)
            .addNode(east, false)
            .addEdge(center, net.minecraft.core.Direction.EAST);
        var events = new NetworkGraphEngineFixtures.Events();

        var engine = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            graph::isLoaded,
            graph::node,
            (pos, node, dir) -> graph.connected(pos, dir),
            (pos, node) -> node.isSubnet(),
            (pos, node, subnet) -> events.discovered.add(pos),
            () -> events.connectFinishedCalls++,
            connected -> {
                events.disconnectCalls++;
                events.lastDisconnectWasConnected = connected;
            }
        );

        assertTrue(engine.connectNext());
        engine.invalidate();
        assertEquals(NetworkGraphEngine.State.CONNECTING, engine.state());
        assertEquals(1, events.disconnectCalls);
        assertFalse(events.lastDisconnectWasConnected);

        while (engine.connectNext()) {
            // step until finished.
        }
        assertEquals(NetworkGraphEngine.State.CONNECTED, engine.state());
        assertTrue(events.discovered.size() >= 3);
        assertTrue(events.connectFinishedCalls >= 1);
    }

    @Test
    void shouldComparePriorityByUuidDeterministically() {
        var center = new net.minecraft.core.BlockPos(0, 0, 0);
        var higher = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000020"),
            center,
            $ -> false,
            $ -> false,
            ($1, $2, $3) -> false,
            ($1, $2) -> false,
            ($1, $2, $3) -> {
            },
            () -> {
            },
            $ -> {
            }
        );
        var lower = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            $ -> false,
            $ -> false,
            ($1, $2, $3) -> false,
            ($1, $2) -> false,
            ($1, $2, $3) -> {
            },
            () -> {
            },
            $ -> {
            }
        );

        assertTrue(lower.comparePriority(higher));
        assertFalse(higher.comparePriority(lower));
    }
}
