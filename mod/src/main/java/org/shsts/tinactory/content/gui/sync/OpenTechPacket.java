package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OpenTechPacket implements IPacket {
    INSTANCE;

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {}

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {}
}
