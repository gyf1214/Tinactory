package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConfig extends INBTSerializable<CompoundTag> {
    boolean contains(IMachineConfigType<?> type);

    default boolean contains(IEntry<? extends IMachineConfigType<?>> type) {
        return contains(type.get());
    }

    <T> Optional<T> get(IMachineConfigType<T> type);

    default <T> Optional<T> get(IEntry<IMachineConfigType<T>> type) {
        return get(type.get());
    }

    <T> Optional<T> get(HolderLookup.Provider provider, ResourceLocation loc);

    void apply(ISetMachineConfigPacket packet);

    boolean contains(String key, int tagType);

    Optional<CompoundTag> getCompound(String key);

    interface Entry<T> extends IEntry<T> {
        IMachineConfigType<T> type();
    }
}
