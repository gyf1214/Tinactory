package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MachineConfig implements INBTSerializable<CompoundTag> {
    private CompoundTag tag = new CompoundTag();

    public Optional<Boolean> getBoolean(String key) {
        return tag.contains(key, Tag.TAG_BYTE) ? Optional.of(tag.getBoolean(key)) : Optional.empty();
    }

    public boolean getBoolean(String key, boolean def) {
        return getBoolean(key).orElse(def);
    }

    public Optional<String> getString(String key) {
        return tag.contains(key, Tag.TAG_STRING) ? Optional.of(tag.getString(key)) : Optional.empty();
    }

    public Optional<ResourceLocation> getLoc(String key) {
        return getString(key).map(ResourceLocation::new);
    }

    public void apply(SetMachinePacket packet) {
        tag.merge(packet.getSets());
        for (var key : packet.getResets()) {
            tag.remove(key);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        return tag.copy();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.tag = tag.copy();
    }
}
