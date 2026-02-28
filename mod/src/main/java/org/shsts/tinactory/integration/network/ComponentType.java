package org.shsts.tinactory.integration.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentType<T extends INetworkComponent>
    extends ForgeRegistryEntry<IComponentType<?>> implements IComponentType<T> {
    private final Class<T> clazz;
    private final NetworkComponent.Factory<T> factory;

    public ComponentType(Class<T> clazz, NetworkComponent.Factory<T> factory) {
        this.clazz = clazz;
        this.factory = factory;
    }

    @Override
    public Class<T> clazz() {
        return clazz;
    }

    @Override
    public T create(INetwork network) {
        return factory.create(this, network);
    }

    private static Collection<IComponentType<?>> componentTypes;

    public static void onBake(IForgeRegistry<IComponentType<?>> registry) {
        componentTypes = registry.getValues();
    }

    public static Collection<IComponentType<?>> getComponentTypes() {
        return componentTypes;
    }
}
