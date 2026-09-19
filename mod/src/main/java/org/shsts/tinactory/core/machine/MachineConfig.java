package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.shsts.tinactory.AllRegistries.MACHINE_CONFIGS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineConfig implements IMachineConfig {
    public record Entry<T>(ResourceLocation loc, IMachineConfigType<T> type, T value)
        implements IMachineConfig.Entry<T> {
        @Override
        public T get() {
            return value;
        }
    }

    private final Map<IMachineConfigType<?>, IMachineConfig.Entry<?>> configs = new IdentityHashMap<>();
    private final Map<String, Tag> unknownTags = new HashMap<>();

    public static <T> Entry<T> fromTag(HolderLookup.Provider provider,
        ResourceLocation loc, IMachineConfigType<T> type, Tag tag) {
        var val = CodecHelper.parseTag(provider, type.codec(), tag);
        return new Entry<>(loc, type, val);
    }

    public static <T> Tag encodeVal(HolderLookup.Provider provider, IMachineConfig.Entry<T> entry) {
        return CodecHelper.encodeTag(provider, entry.type().codec(), entry.get());
    }

    public static <T> Entry<T> fromStream(RegistryFriendlyByteBuf buf, ResourceLocation loc,
        IMachineConfigType<T> type) {
        var val = type.streamCodec().decode(buf);
        return new Entry<>(loc, type, val);
    }

    public static <T> void encodeVal(RegistryFriendlyByteBuf buf, IMachineConfig.Entry<T> entry) {
        entry.type().streamCodec().encode(buf, entry.get());
    }

    @Override
    public boolean contains(IMachineConfigType<?> type) {
        return configs.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(IMachineConfigType<T> type) {
        return Optional.ofNullable(configs.get(type)).map($ -> (T) $.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(HolderLookup.Provider provider, ResourceLocation loc) {
        return CodecHelper.lookupHolder(provider, MACHINE_CONFIGS.key(), loc)
            .flatMap($ -> get((IMachineConfigType<T>) $.value()));
    }

    @Override
    public void apply(ISetMachineConfigPacket packet) {
        for (var set : packet.getSets()) {
            configs.put(set.type(), set);
        }
        for (var reset : packet.getResets()) {
            configs.remove(reset);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        for (var entry : configs.values()) {
            tag.put(entry.loc().toString(), encodeVal(provider, entry));
        }
        for (var entry : unknownTags.entrySet()) {
            tag.put(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    private static Optional<? extends Holder<IMachineConfigType<?>>> lookupType(
        HolderLookup.RegistryLookup<IMachineConfigType<?>> lookup, String key) {
        if (key.contains(":")) {
            var holder = lookup.get(ResourceKey.create(MACHINE_CONFIGS.key(), ResourceLocation.parse(key)));
            if (holder.isPresent()) {
                return holder;
            }
        }
        return Optional.empty();
    }

    private <T> void fixLegacyConfig(HolderLookup.Provider provider, ResourceLocation loc,
        IMachineConfigType<T> type) {
        type.fixLegacyConfig(provider, unknownTags)
            .ifPresent(val -> {
                if (!configs.containsKey(type)) {
                    configs.put(type, new Entry<>(loc, type, val));
                }
            });
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        configs.clear();
        unknownTags.clear();
        var lookup = provider.lookupOrThrow(MACHINE_CONFIGS.key());
        for (var key : tag.getAllKeys()) {
            var val = Objects.requireNonNull(tag.get(key));
            var typeHolder = lookupType(lookup, key);
            if (typeHolder.isPresent()) {
                var holder = typeHolder.get();
                var loc = holder.unwrapKey().orElseThrow().location();
                var type = holder.value();
                configs.put(holder.value(), fromTag(provider, loc, type, val));
            } else {
                unknownTags.put(key, val);
            }
        }
        lookup.listElements()
            .forEach(holder -> fixLegacyConfig(provider, holder.unwrapKey().orElseThrow().location(), holder.value()));
    }
}
