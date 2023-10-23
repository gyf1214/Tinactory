package org.shsts.tinactory.registrate.handler;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.builder.RegistryEntryBuilder;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryEntryHandler<T extends IForgeRegistryEntry<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<RegistryEntryBuilder<T, ?, ?, ?>> builders = new ArrayList<>();

    public <U extends T> RegistryEntry<U> getEntry(String id) {
        return this.getEntry(new ResourceLocation(id));
    }

    public abstract <U extends T> RegistryEntry<U> getEntry(ResourceLocation loc);

    public abstract Class<T> getEntryClass();

    public <U extends T> RegistryEntry<U> register(RegistryEntryBuilder<T, U, ?, ?> builder) {
        builders.add(builder);
        return new RegistryEntry<>(builder.loc.getNamespace(), builder.id);
    }

    private void onRegisterEvent(RegistryEvent.Register<T> event) {
        var registry = event.getRegistry();
        LOGGER.info("Registry {} register {} objects", registry.getRegistryName(), this.builders.size());
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

        public Forge(IForgeRegistry<T1> registry) {
            this.registry = () -> registry;
            this.entryClass = registry.getRegistrySuperType();
        }

        public Forge(Class<T1> entryClass, Supplier<IForgeRegistry<T1>> registry) {
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
    forge(IForgeRegistry<T1> forgeRegistry) {
        return new Forge<>(forgeRegistry);
    }

    public static <T1 extends IForgeRegistryEntry<T1>> RegistryEntryHandler<T1>
    forge(Class<T1> entryClass, Supplier<IForgeRegistry<T1>> forgeRegistry) {
        return new Forge<>(entryClass, forgeRegistry);
    }
}
