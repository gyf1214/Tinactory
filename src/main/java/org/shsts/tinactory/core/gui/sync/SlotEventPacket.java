package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlotEventPacket extends MenuEventPacket {
    private int index;
    private int button;

    public SlotEventPacket() {}

    public SlotEventPacket(int containerId, int eventId, int index, int button) {
        super(containerId, eventId);
        this.index = index;
        this.button = button;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeVarInt(index);
        buf.writeVarInt(button);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
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
