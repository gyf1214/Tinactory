package org.shsts.tinactory.core.tech;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
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
        buf.writeMap(progress, (buf1, loc) -> buf1.writeResourceLocation(loc),
            (buf1, value) -> buf1.writeLong(value));
        buf.writeBoolean(updateTarget);
        if (updateTarget) {
            buf.writeOptional(Optional.ofNullable(targetTech), (buf1, loc) -> buf1.writeResourceLocation(loc));
        }
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        progress = buf.readMap(buf1 -> buf1.readResourceLocation(), buf1 -> buf1.readLong());
        updateTarget = buf.readBoolean();
        if (updateTarget) {
            targetTech = buf.readOptional(buf1 -> buf1.readResourceLocation()).orElse(null);
        }
    }
}
