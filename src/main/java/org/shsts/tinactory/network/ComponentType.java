package org.shsts.tinactory.network;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class ComponentType<T extends Component> extends ForgeRegistryEntry<ComponentType<?>> {
    public final Class<T> componentClass;
    protected final Component.Factory<T> factory;

    public ComponentType(Class<T> componentClass, Component.Factory<T> factory) {
        this.componentClass = componentClass;
        this.factory = factory;
    }

    public T create(Network network) {
        return this.factory.create(this, network);
    }
}
