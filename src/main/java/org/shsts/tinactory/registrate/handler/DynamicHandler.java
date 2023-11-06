package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DynamicHandler<T extends IForgeRegistryEntry<T>> {
    private final Class<T> entryClass;
    private final Supplier<T> dummyFactory;
    private final Set<ResourceLocation> locations = new HashSet<>();

    public DynamicHandler(Class<T> entryClass, Supplier<T> dummyFactory) {
        this.entryClass = entryClass;
        this.dummyFactory = dummyFactory;
    }

    public void addLocation(ResourceLocation loc) {
        this.locations.add(loc);
    }

    private T dummy(ResourceLocation loc) {
        var object = this.dummyFactory.get();
        object.setRegistryName(loc);
        return object;
    }

    private void onRegisterEvent(RegistryEvent.Register<T> event) {
        var registry = event.getRegistry();
        for (var loc : this.locations) {
            registry.register(this.dummy(loc));
        }
    }

    public void addListener(IEventBus modEventBus) {
        modEventBus.addGenericListener(this.entryClass, this::onRegisterEvent);
    }
}
