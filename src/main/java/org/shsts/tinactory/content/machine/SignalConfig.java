package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record SignalConfig(UUID machine, String key) {
    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.putUUID("machine", machine);
        tag.putString("key", key);
        return tag;
    }

    public static SignalConfig fromTag(CompoundTag tag) {
        return new SignalConfig(tag.getUUID("machine"), tag.getString("key"));
    }
}
