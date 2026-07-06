package org.shsts.tinactory.unit.fixture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

public enum TestRegistry implements HolderLookup.Provider {
    TEST_REGISTRY;

    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(
        ResourceKey<? extends Registry<? extends T>> registryKey) {
        throw new UnsupportedOperationException();
    }
}
