package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidEventPacket extends MenuEventPacket {
    private int tankIndex;
    private int button;

    public FluidEventPacket() {}

    public FluidEventPacket(int containerId, int eventId, int tankIndex, int button) {
        super(containerId, eventId);
        this.tankIndex = tankIndex;
        this.button = button;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeVarInt(tankIndex);
        buf.writeVarInt(button);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        tankIndex = buf.readVarInt();
        button = buf.readVarInt();
    }

    public int getTankIndex() {
        return tankIndex;
    }

    public int getButton() {
        return button;
    }
}
