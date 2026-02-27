package org.shsts.tinactory.unit.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.network.NetworkGraphEngine;
import org.shsts.tinactory.core.network.NetworkManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkManagerTest {
    @Test
    void shouldPutAndGetNetworkByPosition() {
        var manager = new NetworkManager();
        var pos = new BlockPos(0, 0, 0);
        var engine = createEngine(pos);

        manager.putNetworkAtPos(pos, engine);

        assertTrue(manager.hasNetworkAtPos(pos));
        assertSame(engine, manager.getNetworkAtPos(pos).orElseThrow());
    }

    @Test
    void shouldInvalidateOwningEngineByPosition() {
        var manager = new NetworkManager();
        var pos = new BlockPos(0, 0, 0);
        var engine = createEngine(pos);

        manager.putNetworkAtPos(pos, engine);
        while (engine.connectNext()) {
            // connect to connected state.
        }
        assertEquals(NetworkGraphEngine.State.CONNECTED, engine.state());

        manager.invalidatePos(pos);

        assertEquals(NetworkGraphEngine.State.CONNECTING, engine.state());
        assertFalse(manager.getNetworkAtPos(pos).isPresent());
    }

    @Test
    void shouldInvalidateBothPositionsByDirection() {
        var manager = new NetworkManager();
        var pos = new BlockPos(0, 0, 0);
        var pos1 = pos.relative(Direction.EAST);
        var engine0 = createEngine(pos);
        var engine1 = createEngine(pos1);

        manager.putNetworkAtPos(pos, engine0);
        manager.putNetworkAtPos(pos1, engine1);
        while (engine0.connectNext()) {
            // connect to connected state.
        }
        while (engine1.connectNext()) {
            // connect to connected state.
        }

        manager.invalidatePosDir(pos, Direction.EAST);

        assertEquals(NetworkGraphEngine.State.CONNECTING, engine0.state());
        assertEquals(NetworkGraphEngine.State.CONNECTING, engine1.state());
    }

    @Test
    void shouldInvalidateSharedRefAcrossMultiplePositions() {
        var manager = new NetworkManager();
        var pos = new BlockPos(0, 0, 0);
        var pos1 = pos.relative(Direction.NORTH);
        var engine = createEngine(pos);

        manager.putNetworkAtPos(pos, engine);
        manager.putNetworkAtPos(pos1, engine);

        manager.invalidatePos(pos);

        assertFalse(manager.hasNetworkAtPos(pos));
        assertFalse(manager.hasNetworkAtPos(pos1));
    }

    private static NetworkGraphEngine<Boolean> createEngine(BlockPos center) {
        var adapter = new NetworkGraphEngine.INetworkGraphAdapter<Boolean>() {
            @Override
            public boolean isNodeLoaded(BlockPos pos) {
                return true;
            }

            @Override
            public Boolean getNodeData(BlockPos pos) {
                return false;
            }

            @Override
            public boolean isConnected(BlockPos pos, Boolean data, Direction dir) {
                return false;
            }

            @Override
            public boolean isSubnet(BlockPos pos, Boolean data) {
                return false;
            }

            @Override
            public void onDiscover(BlockPos pos, Boolean data, BlockPos subnet) {
            }

            @Override
            public void onConnectFinished() {
            }

            @Override
            public void onDisconnect(boolean connected) {
            }
        };
        return new NetworkGraphEngine<>(
            UUID.fromString("00000000-0000-0000-0000-000000000010"),
            center,
            adapter
        );
    }
}
