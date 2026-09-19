package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {}
}
