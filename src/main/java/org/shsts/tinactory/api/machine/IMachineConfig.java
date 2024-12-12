package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConfig extends INBTSerializable<CompoundTag> {
    void apply(ISetMachineConfigPacket packet);

    boolean contains(String key, int tagType);

    boolean getBoolean(String key);

    Optional<String> getString(String key);

    default Optional<ResourceLocation> getLoc(String key) {
        return getString(key).map(ResourceLocation::new);
    }

    Optional<CompoundTag> getCompound(String key);
}
