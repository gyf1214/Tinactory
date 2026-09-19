package org.shsts.tinactory.core.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record EntryCodec<E>(ResourceKey<? extends Registry<E>> registryKey) implements Codec<IEntry<E>> {
    /**
     * Assuming the IEntry is valid.
     */
    @Override
    public <T> DataResult<T> encode(IEntry<E> input, DynamicOps<T> ops, T prefix) {
        return ResourceLocation.CODEC.encode(input.loc(), ops, prefix);
    }

    @Override
    public <T> DataResult<Pair<IEntry<E>, T>> decode(DynamicOps<T> ops, T input) {
        if (!(ops instanceof RegistryOps<?> registryOps)) {
            return DataResult.error(() -> "Can't access registries");
        }
        var info = registryOps.lookupProvider.lookup(registryKey);
        if (info.isEmpty()) {
            return DataResult.error(() -> "Can't access registry " + registryKey);
        }
        var lifecycle = info.get().elementsLifecycle();
        var getter = info.get().getter();
        return ResourceLocation.CODEC.decode(ops, input)
            .flatMap(res -> getter.get(ResourceKey.create(registryKey, res.getFirst()))
                .map(holder -> CORE.createEntry(holder.unwrapKey().orElseThrow().location(), holder.value()))
                .map(entry -> DataResult.success(Pair.of(entry, res.getSecond()), lifecycle))
                .orElseGet(() -> DataResult.error(() -> "Failed to get element " + res.getFirst())));
    }
}
