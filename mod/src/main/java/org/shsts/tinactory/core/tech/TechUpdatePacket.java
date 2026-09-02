package org.shsts.tinactory.core.tech;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechUpdatePacket implements IPacket {
    public enum UpdateType {
        FULL,
        INCREMENTAL,
        CLEAR
    }

    @Nullable
    private String profileId;
    private UpdateType updateType;
    private Map<ResourceLocation, Long> progress = Map.of();
    private boolean updateTarget;
    @Nullable
    private ResourceLocation targetTech;
    @Nullable
    private Component displayName;

    public TechUpdatePacket() {}

    private TechUpdatePacket(@Nullable String profileId, UpdateType updateType,
        Map<ResourceLocation, Long> progress, boolean updateTarget,
        @Nullable ResourceLocation targetTech, @Nullable Component displayName) {
        this.profileId = profileId;
        this.updateType = updateType;
        this.progress = progress;
        this.updateTarget = updateTarget;
        this.targetTech = targetTech;
        this.displayName = displayName;
    }

    public static TechUpdatePacket incremental(String profileId, Map<ResourceLocation, Long> progress) {
        return new TechUpdatePacket(profileId, UpdateType.INCREMENTAL, progress, false, null, null);
    }

    public static TechUpdatePacket progress(String profileId, ResourceLocation tech, long progress) {
        return incremental(profileId, Map.of(tech, progress));
    }

    public static TechUpdatePacket target(String profileId, @Nullable ResourceLocation tech) {
        return new TechUpdatePacket(profileId, UpdateType.INCREMENTAL, Map.of(), true, tech, null);
    }

    public static TechUpdatePacket full(String profileId, Map<ResourceLocation, Long> progress,
        @Nullable ResourceLocation targetTech, Component displayName) {
        return new TechUpdatePacket(profileId, UpdateType.FULL, progress, true, targetTech, displayName);
    }

    public static TechUpdatePacket clear() {
        return new TechUpdatePacket(null, UpdateType.CLEAR, Map.of(), false, null, null);
    }

    public Map<ResourceLocation, Long> getProgress() {
        return progress;
    }

    public Optional<String> getProfileId() {
        return Optional.ofNullable(profileId);
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public Optional<ResourceLocation> getTargetTech() {
        return Optional.ofNullable(targetTech);
    }

    public boolean isUpdateTarget() {
        return updateTarget;
    }

    public Component getDisplayName() {
        assert displayName != null;
        return displayName;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(profileId), FriendlyByteBuf::writeUtf);
        buf.writeEnum(updateType);
        buf.writeMap(progress, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeLong);
        buf.writeBoolean(updateTarget);
        if (updateTarget) {
            buf.writeOptional(Optional.ofNullable(targetTech), FriendlyByteBuf::writeResourceLocation);
        }
        CodecHelper.encodeOptionalToBuf(buf, Optional.ofNullable(displayName),
            ComponentSerialization.STREAM_CODEC);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        profileId = buf.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        updateType = buf.readEnum(UpdateType.class);
        progress = buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readLong);
        updateTarget = buf.readBoolean();
        if (updateTarget) {
            targetTech = buf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null);
        }
        displayName = CodecHelper.parseOptionalFromBuf(buf, ComponentSerialization.STREAM_CODEC)
            .orElse(null);
    }
}
