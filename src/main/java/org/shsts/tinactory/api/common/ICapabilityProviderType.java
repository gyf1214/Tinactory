package org.shsts.tinactory.api.common;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Function;

public interface ICapabilityProviderType<T extends BlockEntity, B extends Function<T, ICapabilityProvider>> extends
        IForgeRegistryEntry<ICapabilityProviderType<?, ?>> {
    B getBuilder();
}
