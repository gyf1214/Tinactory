package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.RegistryBuilderWrapper;
import org.shsts.tinactory.registrate.common.SmartRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
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

    public <T extends IForgeRegistryEntry<T>> SmartRegistry<T>
    register(RegistryBuilderWrapper<T, ?> builder) {
        this.builders.add(builder);
        return new SmartRegistry<>(this.registrate.modid, builder.id);
    }

    public void onNewRegistry(NewRegistryEvent event) {
        for (var builder : this.builders) {
            builder.registerObject(event);
        }
        // free reference
        this.builders.clear();
    }
}
