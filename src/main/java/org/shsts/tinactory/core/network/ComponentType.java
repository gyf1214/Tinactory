package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentType<T extends NetworkComponent> extends ForgeRegistryEntry<ComponentType<?>> {
    private final Class<T> componentClass;
    private final NetworkComponent.Factory<T> factory;

    public ComponentType(Class<T> componentClass, NetworkComponent.Factory<T> factory) {
        this.componentClass = componentClass;
        this.factory = factory;
    }

    public T create(Network network) {
        return factory.create(this, network);
    }

    public T cast(NetworkComponent networkComponent) {
        return componentClass.cast(networkComponent);
    }

    private static Collection<ComponentType<?>> componentTypes;

    public static void onBake(IForgeRegistry<ComponentType<?>> registry) {
        componentTypes = registry.getValues();
    }

    public static Collection<ComponentType<?>> getComponentTypes() {
        return componentTypes;
    }
}
