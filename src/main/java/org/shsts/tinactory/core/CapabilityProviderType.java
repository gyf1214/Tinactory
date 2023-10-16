package org.shsts.tinactory.core;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CapabilityProviderType<T extends BlockEntity, U extends ICapabilityProvider> extends
        ForgeRegistryEntry<CapabilityProviderType<?, ?>> {
    protected final Function<T, U> factory;

    public CapabilityProviderType(Function<T, U> factory) {
        this.factory = factory;
    }

    public U create(T be) {
        return this.factory.apply(be);
    }
}
