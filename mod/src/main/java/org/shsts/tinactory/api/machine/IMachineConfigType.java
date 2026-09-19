package org.shsts.tinactory.api.machine;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConfigType<T> {
    Codec<T> codec();

    default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return ByteBufCodecs.fromCodecWithRegistries(codec());
    }

    Optional<String> legacyKey();
}
