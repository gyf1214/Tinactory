package org.shsts.tinactory.core.tech;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechUpdatePacket implements IPacket {
    private Map<ResourceLocation, Long> progress;
    private boolean updateTarget;
    @Nullable
    private ResourceLocation targetTech;

    public TechUpdatePacket() {}

    private TechUpdatePacket(Map<ResourceLocation, Long> progress, boolean updateTarget,
        @Nullable ResourceLocation targetTech) {
        this.progress = progress;
        this.updateTarget = updateTarget;
        this.targetTech = targetTech;
    }

    public static TechUpdatePacket progress(Map<ResourceLocation, Long> progress) {
        return new TechUpdatePacket(progress, false, null);
    }

    public static TechUpdatePacket progress(ResourceLocation tech, long progress) {
        return progress(Map.of(tech, progress));
    }

    public static TechUpdatePacket target(@Nullable ResourceLocation tech) {
        return new TechUpdatePacket(Map.of(), true, tech);
    }

    public static TechUpdatePacket full(Map<ResourceLocation, Long> progress,
        @Nullable ResourceLocation targetTech) {
        return new TechUpdatePacket(progress, true, targetTech);
    }

    public Map<ResourceLocation, Long> getProgress() {
        return progress;
    }

    public Optional<ResourceLocation> getTargetTech() {
        return Optional.ofNullable(targetTech);
    }

    public boolean isUpdateTarget() {
        return updateTarget;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeMap(progress, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeLong);
        buf.writeBoolean(updateTarget);
        if (updateTarget) {
            buf.writeOptional(Optional.ofNullable(targetTech), FriendlyByteBuf::writeResourceLocation);
        }
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        progress = buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readLong);
        updateTarget = buf.readBoolean();
        if (updateTarget) {
            targetTech = buf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null);
        }
    }
}
