package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EmptyMachineConfig implements IMachineConfig {
    @Override
    public boolean contains(IMachineConfigType<?> type) {
        return false;
    }

    @Override
    public <T> Optional<T> get(IMachineConfigType<T> type) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> get(HolderLookup.Provider provider, ResourceLocation loc) {
        return Optional.empty();
    }

    @Override
    public void apply(ISetMachineConfigPacket packet) {}

    @Override
    public boolean contains(String key, int tagType) {
        return false;
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getInt(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Long> getLong(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getString(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Tag> getTag(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<ListTag> getList(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<CompoundTag> getCompound(String key) {
        return Optional.empty();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {}
}
