package org.shsts.tinactory.core.machine;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineConfigType<T> implements IMachineConfigType<T> {
    private final Codec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
    @Nullable
    private final String legacyKey;

    public MachineConfigType(Codec<T> codec, @Nullable String legacyKey) {
        this.codec = codec;
        this.streamCodec = ByteBufCodecs.fromCodecWithRegistries(codec);
        this.legacyKey = legacyKey;
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }

    @Override
    public Optional<T> fixLegacyConfig(HolderLookup.Provider provider, Map<String, Tag> unknownTags) {
        if (legacyKey == null) {
            return Optional.empty();
        }
        if (unknownTags.containsKey(legacyKey)) {
            var val = CodecHelper.parseTag(provider, codec, unknownTags.get(legacyKey));
            unknownTags.remove(legacyKey);
            return Optional.of(val);
        }
        return Optional.empty();
    }
}
