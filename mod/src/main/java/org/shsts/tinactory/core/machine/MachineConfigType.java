package org.shsts.tinactory.core.machine;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.shsts.tinactory.api.machine.IMachineConfigType;

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
    public Optional<String> legacyKey() {
        return Optional.ofNullable(legacyKey);
    }
}
