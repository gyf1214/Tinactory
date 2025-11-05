package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OpenTechPacket implements IPacket {
    INSTANCE;

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {}

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {}
}
