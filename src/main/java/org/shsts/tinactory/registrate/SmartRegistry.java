package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmartRegistry<T extends IForgeRegistryEntry<T>> extends RegistryEntry<IForgeRegistry<T>> {
    @Nullable
    private RegistryEntryHandler<T> handler;

    public SmartRegistry(String modid, String id) {
        super(modid, id);
    }

    public void setHandler(RegistryEntryHandler<T> handler) {
        this.handler = handler;
    }

    public RegistryEntryHandler<T> getHandler() {
        assert this.handler != null;
        return this.handler;
    }
}
