package org.shsts.tinactory.core.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlotEventPacket implements IPacket {
    private int index;
    private int button;

    public SlotEventPacket() {}

    public SlotEventPacket(int index, int button) {
        this.index = index;
        this.button = button;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeVarInt(button);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        index = buf.readVarInt();
        button = buf.readVarInt();
    }

    public int getIndex() {
        return index;
    }

    public int getButton() {
        return button;
    }
}
