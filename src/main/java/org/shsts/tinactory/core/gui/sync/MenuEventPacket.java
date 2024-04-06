package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.common.IPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuEventPacket implements IPacket {
    protected int containerId;
    protected int eventId;

    public int getContainerId() {
        return containerId;
    }

    public int getEventId() {
        return eventId;
    }

    public MenuEventPacket() {}

    public MenuEventPacket(int containerId, int eventId) {
        this.containerId = containerId;
        this.eventId = eventId;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(eventId);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        eventId = buf.readVarInt();
    }

    @FunctionalInterface
    public interface Factory<P extends MenuEventPacket> {
        P create(int containerId, int eventId);
    }
}
