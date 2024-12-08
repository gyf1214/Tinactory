package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.gui.sync.MenuEventPacket;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachineConfigPacket1 extends MenuEventPacket {
    private CompoundTag sets;
    private List<String> resets;

    public SetMachineConfigPacket1() {}

    private SetMachineConfigPacket1(int containerId, int eventId, Builder builder) {
        super(containerId, eventId);
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
        super.serializeToBuf(buf);
        buf.writeNbt(sets);
        buf.writeCollection(resets, FriendlyByteBuf::writeUtf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        sets = buf.readNbt();
        resets = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
    }

    public static class Builder implements MenuEventPacket.Factory<SetMachineConfigPacket1> {
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
        public SetMachineConfigPacket1 create(int containerId, int eventId) {
            return new SetMachineConfigPacket1(containerId, eventId, this);
        }

        public SetMachineConfigPacket1 create() {
            // where containerId and eventId are irrelevant
            return create(0, 0);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
