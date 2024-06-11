package org.shsts.tinactory.registrate.context;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryDataContext<T extends IForgeRegistryEntry<T>, U extends T, P extends DataProvider>
        extends DataContext<P> {
    public final String id;
    public final U object;

    public RegistryDataContext(String modid, P provider, String id, U object) {
        super(modid, provider);
        this.id = id;
        this.object = object;
    }

    public RegistryDataContext(RegistryEntry<U> entry, P provider) {
        super(entry.modid, provider);
        this.id = entry.id;
        this.object = entry.get();
    }
}
