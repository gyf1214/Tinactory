package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineConfig implements IMachineConfig {
    private CompoundTag tag = new CompoundTag();

    @Override
    public boolean contains(String key, int tagType) {
        return tag.contains(key, tagType);
    }

    @Override
    public Optional<String> getString(String key) {
        return tag.contains(key, Tag.TAG_STRING) ? Optional.of(tag.getString(key)) :
            Optional.empty();
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return tag.contains(key, Tag.TAG_BYTE) ? Optional.of(tag.getBoolean(key)) : Optional.empty();
    }

    @Override
    public Optional<CompoundTag> getCompound(String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(tag.getCompound(key)) :
            Optional.empty();
    }

    @Override
    public void apply(ISetMachineConfigPacket packet) {
        var sets = packet.getSets();
        for (var k : sets.getAllKeys()) {
            var v = sets.get(k);
            assert v != null;
            tag.put(k, v.copy());
        }
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
