package org.shsts.tinactory.core;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IPacket {
    void serializeToBuf(FriendlyByteBuf buf);
}
