package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.builder.RegistryEntryBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryEntryHandler<T extends IForgeRegistryEntry<T>> {
    private final Registrate registrate;
    private final List<RegistryEntryBuilder<T, ?, ?, ?>> builders = new ArrayList<>();

    public RegistryEntryHandler(Registrate registrate) {
        this.registrate = registrate;
    }

    public <U extends T> RegistryEntry<U> getEntry(String id) {
        return this.getEntry(new ResourceLocation(this.registrate.modid, id));
    }

    public abstract <U extends T> RegistryEntry<U> getEntry(ResourceLocation loc);

    public abstract Class<T> getEntryClass();

    public <U extends T> RegistryEntry<U> register(RegistryEntryBuilder<T, U, ?, ?> builder) {
        builders.add(builder);
        return new RegistryEntry<>(this.registrate.modid, builder.id);
    }

    private void onRegisterEvent(RegistryEvent.Register<T> event) {
        var registry = event.getRegistry();
        for (var builder : this.builders) {
            builder.registerObject(registry);
        }
        // free reference
        this.builders.clear();
    }

    public void addListener(IEventBus modEventBus) {
        modEventBus.addGenericListener(this.getEntryClass(), this::onRegisterEvent);
    }

    private static class Forge<T1 extends IForgeRegistryEntry<T1>> extends RegistryEntryHandler<T1> {
        private final Supplier<IForgeRegistry<T1>> registry;
        private final Class<T1> entryClass;

        public Forge(Registrate registrate, IForgeRegistry<T1> registry) {
            super(registrate);
            this.registry = () -> registry;
            this.entryClass = registry.getRegistrySuperType();
        }

        public Forge(Registrate registrate, Class<T1> entryClass, Supplier<IForgeRegistry<T1>> registry) {
            super(registrate);
            this.registry = registry;
            this.entryClass = entryClass;
        }

        @Override
        public <U extends T1> RegistryEntry<U> getEntry(ResourceLocation loc) {
            return new RegistryEntry<>(loc, () -> RegistryObject.<T1, U>create(loc, this.registry.get()).get());
        }

        @Override
        public Class<T1> getEntryClass() {
            return this.entryClass;
        }

    }

    public static <T1 extends IForgeRegistryEntry<T1>> RegistryEntryHandler<T1>
    forge(Registrate registrate, IForgeRegistry<T1> forgeRegistry) {
        return new Forge<>(registrate, forgeRegistry);
    }

    public static <T1 extends IForgeRegistryEntry<T1>> RegistryEntryHandler<T1>
    forge(Registrate registrate, Class<T1> entryClass, Supplier<IForgeRegistry<T1>> forgeRegistry) {
        return new Forge<>(registrate, entryClass, forgeRegistry);
    }
}
