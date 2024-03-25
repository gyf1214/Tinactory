package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.gui.sync.ContainerEventPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachineEventPacket extends ContainerEventPacket {
    private @Nullable Boolean autoDumpItem = null;
    private @Nullable Boolean autoDumpFluid = null;

    public SetMachineEventPacket() {}

    private SetMachineEventPacket(int containerId, int eventId, Builder builder) {
        super(containerId, eventId);
        this.autoDumpItem = builder.autoDumpItem;
        this.autoDumpFluid = builder.autoDumpFluid;
    }

    public Optional<Boolean> getAutoDumpItem() {
        return Optional.ofNullable(this.autoDumpItem);
    }

    public Optional<Boolean> getAutoDumpFluid() {
        return Optional.ofNullable(this.autoDumpFluid);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeOptional(Optional.ofNullable(this.autoDumpItem), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(this.autoDumpFluid), FriendlyByteBuf::writeBoolean);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        this.autoDumpItem = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        this.autoDumpFluid = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
    }

    public static class Builder implements ContainerEventPacket.Factory<SetMachineEventPacket> {
        private @Nullable Boolean autoDumpItem = null;
        private @Nullable Boolean autoDumpFluid = null;

        public Builder autoDumpItem(boolean value) {
            this.autoDumpItem = value;
            return this;
        }

        public Builder autoDumpFluid(boolean value) {
            this.autoDumpFluid = value;
            return this;
        }

        @Override
        public SetMachineEventPacket create(int containerId, int eventId) {
            return new SetMachineEventPacket(containerId, eventId, this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
