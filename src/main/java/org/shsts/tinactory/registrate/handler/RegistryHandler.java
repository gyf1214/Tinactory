package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.builder.RegistryBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryHandler<T extends IForgeRegistryEntry<T>> {
    private final Registrate registrate;
    private final List<RegistryBuilder<T, ?, ?, ?>> builders = new ArrayList<>();

    public RegistryHandler(Registrate registrate) {
        this.registrate = registrate;
    }

    public <U extends T> RegistryEntry<U> getEntry(String id) {
        return this.getEntry(new ResourceLocation(this.registrate.modid, id));
    }

    public abstract <U extends T> RegistryEntry<U> getEntry(ResourceLocation loc);

    public <U extends T> RegistryEntry<U> register(RegistryBuilder<T, U, ?, ?> builder) {
        builders.add(builder);
        return new RegistryEntry<>(this.registrate.modid, builder.id);
    }

    public void onRegisterEvent(RegistryEvent.Register<T> event) {
        var registry = event.getRegistry();
        for (var builder : this.builders) {
            builder.registerObject(registry);
        }
        // free reference
        this.builders.clear();
    }

    private static class Forge<T1 extends IForgeRegistryEntry<T1>> extends RegistryHandler<T1> {
        private final IForgeRegistry<T1> registry;

        public Forge(Registrate registrate, IForgeRegistry<T1> registry) {
            super(registrate);
            this.registry = registry;
        }

        @Override
        public <U extends T1> RegistryEntry<U> getEntry(ResourceLocation loc) {
            var registryObject = RegistryObject.<T1, U>create(loc, this.registry);
            return new RegistryEntry<>(loc, registryObject);
        }
    }

    public static <T1 extends IForgeRegistryEntry<T1>>
    RegistryHandler<T1> forge(Registrate registrate, IForgeRegistry<T1> forgeRegistry) {
        return new Forge<>(registrate, forgeRegistry);
    }
}
