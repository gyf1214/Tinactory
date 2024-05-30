package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.gui.sync.MenuEventPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachinePacket extends MenuEventPacket {
    private CompoundTag sets;
    private List<String> resets;

    public SetMachinePacket() {}

    private SetMachinePacket(int containerId, int eventId, Builder builder) {
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

    public boolean contains(String key) {
        return sets.contains(key) || resets.contains(key);
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

    public static class Builder implements MenuEventPacket.Factory<SetMachinePacket> {
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

        @Override
        public SetMachinePacket create(int containerId, int eventId) {
            return new SetMachinePacket(containerId, eventId, this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
