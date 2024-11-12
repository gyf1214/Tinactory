package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

@ParametersAreNonnullByDefault
public interface IPacket {
    void serializeToBuf(FriendlyByteBuf buf);

    void deserializeFromBuf(FriendlyByteBuf buf);
}
