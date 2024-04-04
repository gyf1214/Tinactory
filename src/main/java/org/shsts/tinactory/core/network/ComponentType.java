package org.shsts.tinactory.core.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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

    private static Collection<ComponentType<?>> componentTypes;

    public static void onBake(IForgeRegistry<ComponentType<?>> registry) {
        componentTypes = registry.getValues();
    }

    public static Collection<ComponentType<?>> getComponentTypes() {
        return componentTypes;
    }
}
