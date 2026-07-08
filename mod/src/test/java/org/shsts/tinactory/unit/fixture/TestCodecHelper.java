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

import java.util.Optional;
import java.util.stream.Stream;

public final class TestCodecHelper {
    private TestCodecHelper() {}

    public static final ITinyCoreLib CORE = ITinyCoreLib.get();

    public static final RegistryAccess TEST_REGISTRY = new RegistryAccess() {
        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> registryKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            throw new UnsupportedOperationException();
        }
    };

    public static RegistryFriendlyByteBuf buf() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), TEST_REGISTRY, ConnectionType.NEOFORGE);
    }

    public static <U> IEntry<U> createEntry(ResourceLocation loc, U obj) {
        return CORE.createEntry(loc, obj);
    }
}
