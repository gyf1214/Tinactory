package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConfig extends INBTSerializable<CompoundTag> {
    void apply(ISetMachineConfigPacket packet);

    boolean contains(String key, int tagType);

    Optional<Boolean> getBoolean(String key);

    default boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key).orElse(defaultValue);
    }

    Optional<Integer> getInt(String key);

    default int getInt(String key, int defaultValue) {
        return getInt(key).orElse(defaultValue);
    }

    Optional<Long> getLong(String key);

    default long getLong(String key, long defaultValue) {
        return getLong(key).orElse(defaultValue);
    }

    Optional<String> getString(String key);

    default Optional<ResourceLocation> getLoc(String key) {
        return getString(key).map(ResourceLocation::parse);
    }

    Optional<Tag> getTag(String key);

    Optional<ListTag> getList(String key);

    default ListTag getCopiedList(String key) {
        return getList(key).map(ListTag::copy).orElseGet(ListTag::new);
    }

    Optional<CompoundTag> getCompound(String key);
}
