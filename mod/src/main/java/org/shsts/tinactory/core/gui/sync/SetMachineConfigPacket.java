package org.shsts.tinactory.core.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachineConfigPacket implements ISetMachineConfigPacket {
    private CompoundTag sets;
    private List<String> resets;

    public SetMachineConfigPacket() {}

    private SetMachineConfigPacket(Builder builder) {
        this.sets = builder.sets;
        this.resets = builder.resets;
    }

    @Override
    public CompoundTag getSets() {
        return sets;
    }

    @Override
    public List<String> getResets() {
        return resets;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(sets);
        buf.writeCollection(resets, (buf1, reset) -> buf1.writeUtf(reset));
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        sets = CodecHelper.readRequiredNbt(buf, "machine config sets");
        resets = buf.readCollection(ArrayList::new, buf1 -> buf1.readUtf());
    }

    public static class Builder implements ISetMachineConfigPacket.Builder {
        private final CompoundTag sets = new CompoundTag();
        private final List<String> resets = new ArrayList<>();

        @Override
        public ISetMachineConfigPacket.Builder reset(String key) {
            resets.add(key);
            return this;
        }

        @Override
        public ISetMachineConfigPacket.Builder set(String key, boolean val) {
            sets.putBoolean(key, val);
            return this;
        }

        @Override
        public ISetMachineConfigPacket.Builder set(String key, int val) {
            sets.putInt(key, val);
            return this;
        }

        @Override
        public ISetMachineConfigPacket.Builder set(String key, String value) {
            sets.putString(key, value);
            return this;
        }

        @Override
        public ISetMachineConfigPacket.Builder set(String key, CompoundTag tag) {
            sets.put(key, tag);
            return this;
        }

        public boolean isEmpty() {
            return sets.isEmpty() && resets.isEmpty();
        }

        @Override
        public ISetMachineConfigPacket get() {
            return new SetMachineConfigPacket(this);
        }
    }

    public static ISetMachineConfigPacket.Builder builder() {
        return new Builder();
    }
}
