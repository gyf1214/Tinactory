package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.common.IPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerEventPacket implements IPacket {
    protected int containerId;
    protected int eventId;

    public int getContainerId() {
        return this.containerId;
    }

    public int getEventId() {
        return this.eventId;
    }

    public ContainerEventPacket() {}

    public ContainerEventPacket(int containerId, int eventId) {
        this.containerId = containerId;
        this.eventId = eventId;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.eventId);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.eventId = buf.readVarInt();
    }

    @FunctionalInterface
    public interface Factory<P extends ContainerEventPacket> {
        P create(int containerId, int eventId);
    }
}
