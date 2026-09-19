package org.shsts.tinactory.core.gui.sync;

import io.netty.handler.codec.DecoderException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.core.machine.MachineConfig;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllRegistries.MACHINE_CONFIGS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetMachineConfigPacket implements ISetMachineConfigPacket {
    private static final StreamCodec<RegistryFriendlyByteBuf, IMachineConfig.Entry<?>> ENTRY_STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, IMachineConfig.Entry<?> entry) {
                buf.writeResourceLocation(entry.loc());
                MachineConfig.encodeVal(buf, entry);
            }

            @Override
            public IMachineConfig.Entry<?> decode(RegistryFriendlyByteBuf buf) {
                var loc = buf.readResourceLocation();
                var registryKey = MACHINE_CONFIGS.key();
                var registry = buf.registryAccess().registry(registryKey).orElse(null);
                if (registry == null) {
                    throw new DecoderException("Cannot access registry " + registryKey);
                }
                var type = registry.get(loc);
                if (type == null) {
                    throw new DecoderException("Failed to get element " + ResourceKey.create(registryKey, loc));
                }
                return MachineConfig.fromStream(buf, loc, type);
            }
        };

    private static final StreamCodec<RegistryFriendlyByteBuf, IMachineConfigType<?>> TYPE_STREAM_CODEC =
        CodecHelper.registryStreamCodec(MACHINE_CONFIGS.key());

    private List<IMachineConfig.Entry<?>> sets;
    private List<IMachineConfigType<?>> resets;

    public SetMachineConfigPacket() {}

    private SetMachineConfigPacket(Builder builder) {
        this.sets = builder.sets;
        this.resets = builder.resets;
    }

    @Override
    public List<? extends IMachineConfig.Entry<?>> getSets() {
        return sets;
    }

    @Override
    public List<IMachineConfigType<?>> getResets() {
        return resets;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, sets, ENTRY_STREAM_CODEC);
        CodecHelper.encodeCollectionToBuf(buf, resets, TYPE_STREAM_CODEC);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        sets = CodecHelper.parseListFromBuf(buf, ENTRY_STREAM_CODEC);
        resets = CodecHelper.parseListFromBuf(buf, TYPE_STREAM_CODEC);
    }

    public static class Builder implements ISetMachineConfigPacket.Builder {
        private final List<IMachineConfig.Entry<?>> sets = new ArrayList<>();
        private final List<IMachineConfigType<?>> resets = new ArrayList<>();

        @SuppressWarnings("unchecked")
        private <T> IMachineConfigType<T> lookupType(HolderLookup.Provider provider, ResourceLocation loc) {
            var key = ResourceKey.create(MACHINE_CONFIGS.key(), loc);
            return (IMachineConfigType<T>) CodecHelper.lookupHolder(provider, key)
                .orElseThrow()
                .value();
        }

        @Override
        public ISetMachineConfigPacket.Builder reset(IMachineConfigType<?> type) {
            resets.add(type);
            return this;
        }

        @Override
        public ISetMachineConfigPacket.Builder reset(HolderLookup.Provider provider, ResourceLocation loc) {
            return reset(lookupType(provider, loc));
        }

        private <T> ISetMachineConfigPacket.Builder set(ResourceLocation loc, IMachineConfigType<T> type,
            T val) {
            sets.add(new MachineConfig.Entry<>(loc, type, val));
            return this;
        }

        @Override
        public <T> ISetMachineConfigPacket.Builder set(IEntry<IMachineConfigType<T>> type, T val) {
            return set(type.loc(), type.get(), val);
        }

        @Override
        public <T> ISetMachineConfigPacket.Builder set(HolderLookup.Provider provider,
            ResourceLocation loc, T val) {
            return set(loc, lookupType(provider, loc), val);
        }

        @Override
        public ISetMachineConfigPacket.Builder reset(String key) {
            return this;
        }

        @Override
        public ISetMachineConfigPacket.Builder set(String key, Tag tag) {
            return this;
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
