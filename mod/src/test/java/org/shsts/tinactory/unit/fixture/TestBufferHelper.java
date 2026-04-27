package org.shsts.tinactory.unit.fixture;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class TestBufferHelper {
    private TestBufferHelper() {}

    public static FriendlyByteBuf buf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}
