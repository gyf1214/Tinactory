package org.shsts.tinactory.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidEventPacket extends ContainerEventPacket {
    protected int tankIndex;
    protected int button;

    public FluidEventPacket() {}

    public FluidEventPacket(int containerId, int eventId, int tankIndex, int button) {
        super(containerId, eventId);
        this.tankIndex = tankIndex;
        this.button = button;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeVarInt(this.tankIndex);
        buf.writeVarInt(this.button);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        this.tankIndex = buf.readVarInt();
        this.button = buf.readVarInt();
    }

    public int getTankIndex() {
        return tankIndex;
    }

    public int getButton() {
        return button;
    }
}
