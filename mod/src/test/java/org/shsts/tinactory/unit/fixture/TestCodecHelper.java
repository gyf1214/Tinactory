package org.shsts.tinactory.unit.fixture;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.shsts.tinycorelib.api.ITinyCoreLib;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class TestCodecHelper {
    private TestCodecHelper() {}

    public static final ITinyCoreLib CORE = ITinyCoreLib.get();

    public static final RegistryAccess EMPTY_REGISTRY = new RegistryAccess() {
        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> registryKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            throw new UnsupportedOperationException();
        }
    };

    public static RegistryAccess createRegistry(Registry<?>... registries) {
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registries));
    }

    public static RegistryFriendlyByteBuf buf(RegistryAccess registry) {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), registry, ConnectionType.NEOFORGE);
    }

    public static RegistryFriendlyByteBuf buf() {
        return buf(EMPTY_REGISTRY);
    }

    public static <U> IEntry<U> createEntry(ResourceLocation loc, U obj) {
        return CORE.createEntry(loc, obj);
    }

    public static <U> IEntry<U> register(Registry<? super U> registry, ResourceLocation loc, U obj) {
        Registry.register(registry, loc, obj);
        return createEntry(loc, obj);
    }

    public static <U> IEntry<U> register(Registry<? super U> registry, String id, U obj) {
        return register(registry, modLoc(id), obj);
    }
}
