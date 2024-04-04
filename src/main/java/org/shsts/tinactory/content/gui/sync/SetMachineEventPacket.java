package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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

    @Nullable
    public Boolean getAutoDumpItem() {
        return autoDumpItem;
    }

    @Nullable
    public Boolean getAutoDumpFluid() {
        return autoDumpFluid;
    }

    public boolean isResetTargetRecipe() {
        return resetTargetRecipe;
    }

    @Nullable
    public ResourceLocation getTargetRecipeLoc() {
        return targetRecipeLoc;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeOptional(Optional.ofNullable(autoDumpItem), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(autoDumpFluid), FriendlyByteBuf::writeBoolean);
        buf.writeBoolean(resetTargetRecipe);
        buf.writeOptional(Optional.ofNullable(targetRecipeLoc), FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        autoDumpItem = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        autoDumpFluid = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        resetTargetRecipe = buf.readBoolean();
        targetRecipeLoc = buf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null);
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
            autoDumpItem = value;
            return this;
        }

        public Builder autoDumpFluid(boolean value) {
            autoDumpFluid = value;
            return this;
        }

        public Builder targetRecipeLoc(ResourceLocation value) {
            targetRecipeLoc = value;
            return this;
        }

        public Builder resetTargetRecipe() {
            resetTargetRecipeLoc = true;
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
