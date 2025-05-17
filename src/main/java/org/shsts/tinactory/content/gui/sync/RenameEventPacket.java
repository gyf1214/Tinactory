package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RenameEventPacket implements IPacket {
    private String name;

    public RenameEventPacket() {}

    public RenameEventPacket(String name) {
        this.name = name;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        name = buf.readUtf();
    }

    public String getName() {
        return name;
    }
}
