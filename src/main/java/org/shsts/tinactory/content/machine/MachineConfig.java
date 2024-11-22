package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MachineConfig implements INBTSerializable<CompoundTag> {
    public enum PortConfig {
        NONE(0), PASSIVE(1), ACTIVE(2);

        public final int index;

        PortConfig(int index) {
            this.index = index;
        }

        public static PortConfig fromIndex(int i) {
            return i >= 0 && i < 3 ? values()[i] : NONE;
        }
    }

    private CompoundTag tag = new CompoundTag();

    public boolean hasString(String key) {
        return tag.contains(key, Tag.TAG_STRING);
    }

    public Optional<String> getString(String key) {
        return tag.contains(key, Tag.TAG_STRING) ? Optional.of(tag.getString(key)) : Optional.empty();
    }

    public Optional<ResourceLocation> getLoc(String key) {
        return getString(key).map(ResourceLocation::new);
    }

    public PortConfig getPortConfig(String key) {
        return PortConfig.fromIndex(tag.contains(key, Tag.TAG_BYTE) ? tag.getByte(key) : 0);
    }

    public boolean getBoolean(String key) {
        return tag.getBoolean(key);
    }

    public Optional<CompoundTag> getCompound(String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(tag.getCompound(key)) : Optional.empty();
    }

    public void apply(SetMachineConfigPacket packet) {
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
