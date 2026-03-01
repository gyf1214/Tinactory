package org.shsts.tinactory.unit.network;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.network.NetworkRuntime;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var subnet = new BlockPos(7, 8, 9);

        runtime.attachComponent(type, ticker -> () -> ticker.tick(null, null));
        runtime.putBlock(block, subnet, component1 -> component1.putBlock(block, null, subnet));

        assertSame(component, runtime.getComponent(type));
        assertSame(subnet, runtime.getSubnet(block));
        assertEquals(1, runtime.allBlocks().size());
        assertTrue(events.contains("component.putBlock:" + block + "->" + subnet));
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
        runtime.putMachine(BlockPos.ZERO, machine);
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
        var subnet = new BlockPos(0, 0, 1);
        var block = new BlockPos(0, 0, 2);

        runtime.putMachine(subnet, machine);
        runtime.putBlock(block, subnet, component -> component.putBlock(block, null, subnet));
        runtime.onDisconnect(true);

        assertTrue(runtime.allMachines().isEmpty());
        assertTrue(runtime.allBlocks().isEmpty());
        assertTrue(events.contains("machine.onDisconnect"));
    }

    @Test
    void shouldNotTouchMinecraftBoundMethodsInRuntimePaths() {
        var events = new ArrayList<String>();
        var scheduling = new NetworkRuntimeFixtures.SchedulingFixture("S");
        var machine = new NetworkRuntimeFixtures.ThrowOnTouchMachine(
            "00000000-0000-0000-0000-000000000333",
            events,
            scheduling
        );
        var runtime = new NetworkRuntime(new NetworkRuntimeFixtures.DummyNetwork(), List.of(scheduling));

        runtime.putMachine(BlockPos.ZERO, machine);
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
}
