package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistryEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IComponentType<T extends INetworkComponent>
    extends IForgeRegistryEntry<IComponentType<?>> {
    Class<T> clazz();

    T create(INetwork network);
}
