package org.shsts.tinactory.unit.network;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.network.ISubnetLabel;
import org.shsts.tinactory.core.network.NetworkRuntime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkRuntimeTest {
    @Test
    void shouldTrackBlocksAndSubnetLookups() {
        var events = new ArrayList<String>();
        var scheduling = new NetworkRuntimeFixtures.SchedulingFixture("S");
        var component = new NetworkRuntimeFixtures.ComponentFixture(events, scheduling);
        var type = new NetworkRuntimeFixtures.ComponentTypeFixture(component);
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of(scheduling));
        var block = new BlockPos(1, 2, 3);
        var subnetA = new BlockPos(7, 8, 9);
        var subnetB = new BlockPos(3, 4, 5);
        var subnets = subnets(subnetA, subnetB);

        runtime.attachComponent(type, ticker -> () -> ticker.tick(null, null));
        runtime.putBlock(block, labels(), subnets,
            component1 -> component1.putBlock(block, null, subnets));

        assertSame(component, runtime.getComponent(type));
        assertSame(subnetA, runtime.getSubnet(block, NetworkGraphEngineFixtures.LABEL_A));
        assertSame(subnetB, runtime.getSubnet(block, NetworkGraphEngineFixtures.LABEL_B));
        assertEquals(List.of(block), List.copyOf(runtime.allBlocks()));
        assertTrue(events.contains("component.putBlock:" + block +
            "->A:" + subnetA + ",B:" + subnetB));
    }

    @Test
    void shouldStoreEachBlockOnceWhenMultipleLabelsArePresent() {
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of());
        var block = new BlockPos(1, 2, 3);

        runtime.putBlock(block, labels(), subnets(new BlockPos(1, 0, 0), new BlockPos(2, 0, 0)),
            component -> {});

        assertEquals(List.of(block), List.copyOf(runtime.allBlocks()));
    }

    @Test
    void shouldTrackMachinesWithoutSubnetKeys() {
        var events = new ArrayList<String>();
        var scheduling = new NetworkRuntimeFixtures.SchedulingFixture("S");
        var pos = new BlockPos(4, 5, 6);
        var machine = new NetworkRuntimeFixtures.MachineFixture(
            "00000000-0000-0000-0000-000000000444",
            events,
            scheduling,
            pos
        );
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of(scheduling));

        runtime.putMachine(machine);

        assertEquals(List.of(machine), List.copyOf(runtime.allMachines()));
        assertEquals(pos, machine.blockPos());
        assertTrue(events.contains("machine.assignNetwork"));
    }

    @Test
    void shouldRunLifecycleAndTickInInjectedSchedulingOrder() {
        var events = new ArrayList<String>();
        var s1 = new NetworkRuntimeFixtures.SchedulingFixture("S1");
        var s2 = new NetworkRuntimeFixtures.SchedulingFixture("S2");
        var component = new NetworkRuntimeFixtures.ComponentFixture(events, s2);
        var type = new NetworkRuntimeFixtures.ComponentTypeFixture(component);
        var machine = new NetworkRuntimeFixtures.MachineFixture(
            "00000000-0000-0000-0000-000000000111",
            events,
            s1
        );
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of(s2, s1));

        runtime.attachComponent(type, ticker -> () -> ticker.tick(null, null));
        runtime.putMachine(machine);
        runtime.onConnectFinished(ticker -> () -> ticker.tick(null, null));
        runtime.tick();

        assertEquals(List.of(
            "machine.assignNetwork",
            "component.onConnect",
            "machine.onConnect",
            "component.onPostConnect",
            "component.tick",
            "machine.tick"
        ), events);
    }

    @Test
    void shouldCleanupOnDisconnect() {
        var events = new ArrayList<String>();
        var scheduling = new NetworkRuntimeFixtures.SchedulingFixture("S");
        var machine = new NetworkRuntimeFixtures.MachineFixture(
            "00000000-0000-0000-0000-000000000222",
            events,
            scheduling
        );
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of(scheduling));
        var block = new BlockPos(0, 0, 2);

        runtime.putMachine(machine);
        runtime.putBlock(block, labels(), subnets(new BlockPos(0, 0, 1), new BlockPos(0, 0, 3)),
            component -> {});
        runtime.onDisconnect(true);

        assertTrue(runtime.allMachines().isEmpty());
        assertTrue(runtime.allBlocks().isEmpty());
        assertNull(runtime.getSubnet(block, NetworkGraphEngineFixtures.LABEL_A));
        assertNull(runtime.getSubnet(block, NetworkGraphEngineFixtures.LABEL_B));
        assertTrue(events.contains("machine.onDisconnect"));
    }

    @Test
    void shouldNotTouchMinecraftBoundMethodsInRuntimePaths() {
        var events = new ArrayList<String>();
        var scheduling = new NetworkRuntimeFixtures.SchedulingFixture("S");
        var machine = new NetworkRuntimeFixtures.MachineFixture(
            "00000000-0000-0000-0000-000000000333",
            events,
            scheduling
        );
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of(scheduling));

        runtime.putMachine(machine);
        runtime.onConnectFinished(ticker -> () -> ticker.tick(null, null));
        runtime.tick();
        runtime.onDisconnect(true);

        assertEquals(List.of(
            "machine.assignNetwork",
            "machine.onConnect",
            "machine.tick",
            "machine.onDisconnect"
        ), events);
    }

    private static Function<ISubnetLabel, BlockPos> subnets(BlockPos subnetA, BlockPos subnetB) {
        return label -> {
            if (label == NetworkGraphEngineFixtures.LABEL_A) {
                return subnetA;
            }
            if (label == NetworkGraphEngineFixtures.LABEL_B) {
                return subnetB;
            }
            throw new IllegalArgumentException("Unknown label " + label);
        };
    }

    private static List<ISubnetLabel> labels() {
        return List.of(NetworkGraphEngineFixtures.LABEL_A, NetworkGraphEngineFixtures.LABEL_B);
    }
}
