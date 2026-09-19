package org.shsts.tinactory.core.util;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record RegistryStreamCodec<T>(ResourceKey<? extends Registry<T>> registryKey) implements
    StreamCodec<RegistryFriendlyByteBuf, T> {
    @Override
    public T decode(RegistryFriendlyByteBuf buf) {
        var loc = buf.readResourceLocation();
        var registry = buf.registryAccess().registry(registryKey)
            .orElseThrow(() -> new DecoderException("Can't access registry " + registryKey));
        var ret = registry.get(loc);
        if (ret == null) {
            throw new DecoderException("Failed to get element " + loc);
        }
        return ret;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, T val) {
        var registry = buf.registryAccess().registry(registryKey)
            .orElseThrow(() -> new EncoderException("Can't access registry " + registryKey));
        var loc = registry.getKey(val);
        if (loc == null) {
            throw new EncoderException("Element " + val + " is not valid in current registry set");
        }
        buf.writeResourceLocation(loc);
    }
}
