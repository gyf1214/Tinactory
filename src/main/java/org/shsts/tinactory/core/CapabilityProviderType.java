package org.shsts.tinactory.core;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CapabilityProviderType<T extends BlockEntity, B extends Function<T, ICapabilityProvider>> extends
        ForgeRegistryEntry<CapabilityProviderType<?, ?>> {
    protected final Supplier<B> builderFactory;

    public CapabilityProviderType(Supplier<B> builderFactory) {
        this.builderFactory = builderFactory;
    }

    public static <T extends BlockEntity>
    CapabilityProviderType<T, ?> simple(Function<T, ? extends ICapabilityProvider> factory) {
        return new CapabilityProviderType<>(() -> factory::apply);
    }

    public B getBuilder() {
        return this.builderFactory.get();
    }
}
