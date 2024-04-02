package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.sync.ContainerEventPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachineEventPacket extends ContainerEventPacket {
    @Nullable
    private Boolean autoDumpItem = null;
    @Nullable
    private Boolean autoDumpFluid = null;
    private boolean resetTargetRecipe = false;
    @Nullable
    private ResourceLocation targetRecipeLoc = null;

    public SetMachineEventPacket() {}

    private SetMachineEventPacket(int containerId, int eventId, Builder builder) {
        super(containerId, eventId);
        this.autoDumpItem = builder.autoDumpItem;
        this.autoDumpFluid = builder.autoDumpFluid;
        this.resetTargetRecipe = builder.resetTargetRecipeLoc;
        this.targetRecipeLoc = builder.targetRecipeLoc;
    }

    public void applyMachine(Machine machine) {
        if (this.autoDumpItem != null) {
            machine.setAutoDumpItem(this.autoDumpItem);
        }
        if (this.autoDumpFluid != null) {
            machine.setAutoDumpFluid(this.autoDumpFluid);
        }
        if (this.resetTargetRecipe) {
            machine.setTargetRecipeLoc(null);
        } else if (this.targetRecipeLoc != null) {
            machine.setTargetRecipeLoc(this.targetRecipeLoc);
        }
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeOptional(Optional.ofNullable(this.autoDumpItem), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(this.autoDumpFluid), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(this.targetRecipeLoc), FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        this.autoDumpItem = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        this.autoDumpFluid = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        this.targetRecipeLoc = buf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null);
    }

    public static class Builder implements ContainerEventPacket.Factory<SetMachineEventPacket> {
        @Nullable
        private Boolean autoDumpItem = null;
        @Nullable
        private Boolean autoDumpFluid = null;
        private boolean resetTargetRecipeLoc = false;
        @Nullable
        private ResourceLocation targetRecipeLoc = null;

        public Builder autoDumpItem(boolean value) {
            this.autoDumpItem = value;
            return this;
        }

        public Builder autoDumpFluid(boolean value) {
            this.autoDumpFluid = value;
            return this;
        }

        public Builder targetRecipeLoc(@Nullable ResourceLocation value) {
            if (value == null) {
                this.resetTargetRecipeLoc = true;
            } else {
                this.targetRecipeLoc = value;
            }
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
