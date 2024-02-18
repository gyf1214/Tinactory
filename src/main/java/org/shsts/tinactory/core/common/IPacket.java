package org.shsts.tinactory.core.common;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IPacket {
    void serializeToBuf(FriendlyByteBuf buf);

    void deserializeFromBuf(FriendlyByteBuf buf);
}
