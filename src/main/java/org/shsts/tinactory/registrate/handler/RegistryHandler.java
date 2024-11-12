package org.shsts.tinactory.registrate.handler;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.RegistryBuilderWrapper;
import org.shsts.tinactory.registrate.common.SmartRegistry;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryHandler {
    private final Registrate registrate;
    private final List<RegistryBuilderWrapper<?, ?>> builders = new ArrayList<>();

    public RegistryHandler(Registrate registrate) {
        this.registrate = registrate;
    }

    public <T extends IForgeRegistryEntry<T>> SmartRegistry<T> register(
        RegistryBuilderWrapper<T, ?> builder) {
        builders.add(builder);
        return new SmartRegistry<>(registrate.modid, builder.id);
    }

    public void onNewRegistry(NewRegistryEvent event) {
        for (var builder : builders) {
            builder.registerObject(event);
        }
        // free reference
        builders.clear();
    }
}
