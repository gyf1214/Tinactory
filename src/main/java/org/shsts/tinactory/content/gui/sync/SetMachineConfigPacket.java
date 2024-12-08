package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachineConfigPacket implements IPacket {
    private CompoundTag sets;
    private List<String> resets;

    public SetMachineConfigPacket() {}

    private SetMachineConfigPacket(Builder builder) {
        this.sets = builder.sets;
        this.resets = builder.resets;
    }

    public CompoundTag getSets() {
        return sets;
    }

    public List<String> getResets() {
        return resets;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeNbt(sets);
        buf.writeCollection(resets, FriendlyByteBuf::writeUtf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        sets = buf.readNbt();
        resets = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
    }

    public static class Builder implements Supplier<SetMachineConfigPacket> {
        private final CompoundTag sets = new CompoundTag();
        private final List<String> resets = new ArrayList<>();

        public Builder reset(String key) {
            resets.add(key);
            return this;
        }

        public Builder set(String key, boolean val) {
            sets.putBoolean(key, val);
            return this;
        }

        public Builder set(String key, ResourceLocation val) {
            sets.putString(key, val.toString());
            return this;
        }

        public Builder set(String key, String value) {
            sets.putString(key, value);
            return this;
        }

        public Builder set(String key, CompoundTag tag) {
            sets.put(key, tag);
            return this;
        }

        public boolean isEmpty() {
            return sets.isEmpty() && resets.isEmpty();
        }

        @Override
        public SetMachineConfigPacket get() {
            return new SetMachineConfigPacket(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
