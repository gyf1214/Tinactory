package org.shsts.tinactory.unit.network;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.network.NetworkGraphEngine;
import org.shsts.tinactory.core.network.NetworkManager;

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
        var manager = new NetworkManager();

        var engine = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, events)
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
        var manager = new NetworkManager();

        var engine = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, events)
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
        var manager = new NetworkManager();

        var engine = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, events)
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
        var graph = new NetworkGraphEngineFixtures.Graph();
        var events = new NetworkGraphEngineFixtures.Events();
        var manager = new NetworkManager();
        var higher = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000020"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, events)
        );
        var lower = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, events)
        );

        assertTrue(lower.comparePriority(higher));
        assertFalse(higher.comparePriority(lower));
    }

    @Test
    void shouldResolveConflictsInEngineAndSkipDiscoverWhenLosingPriority() {
        var center = new net.minecraft.core.BlockPos(0, 0, 0);
        var graph = new NetworkGraphEngineFixtures.Graph().addNode(center, false);
        var manager = new NetworkManager();
        var winnerEvents = new NetworkGraphEngineFixtures.Events();
        var loserEvents = new NetworkGraphEngineFixtures.Events();
        var winner = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, winnerEvents)
        );
        var loser = new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000020"),
            center,
            manager,
            new NetworkGraphEngineFixtures.RecordingAdapter(graph, loserEvents)
        );

        assertTrue(winner.connectNext());
        assertFalse(loser.connectNext());
        assertEquals(winner, manager.getNetworkAtPos(center).orElseThrow());
        assertEquals(1, winnerEvents.discovered.size());
        assertTrue(loserEvents.discovered.isEmpty());
        assertEquals(1, loserEvents.disconnectCalls);
        assertEquals(NetworkGraphEngine.State.CONNECTING, loser.state());
    }
}
