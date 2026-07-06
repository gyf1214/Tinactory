package org.shsts.tinactory.unit.fixture;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

public enum TestRegistry implements RegistryAccess {
    TEST_REGISTRY;

    @Override
    public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> registryKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<RegistryEntry<?>> registries() {
        throw new UnsupportedOperationException();
    }
}
