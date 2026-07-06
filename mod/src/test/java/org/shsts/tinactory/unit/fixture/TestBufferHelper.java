package org.shsts.tinactory.unit.fixture;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;

import static org.shsts.tinactory.unit.fixture.TestRegistry.TEST_REGISTRY;

public final class TestBufferHelper {
    private TestBufferHelper() {}

    public static RegistryFriendlyByteBuf buf() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), TEST_REGISTRY, ConnectionType.NEOFORGE);
    }
}
