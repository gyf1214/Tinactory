package org.shsts.tinactory.registrate.context;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryDataContext<T extends IForgeRegistryEntry<T>, U extends T, P extends DataProvider>
        extends DataContext<P> {
    public final String id;
    public final U object;

    private RegistryDataContext(String modid, P provider, String id, U object) {
        super(modid, provider);
        this.id = id;
        this.object = object;
    }

    public RegistryDataContext(RegistryEntry<U> entry, P provider) {
        super(entry.modid, provider);
        this.id = entry.id;
        this.object = entry.get();
    }

    @SuppressWarnings("unchecked")
    public <U1 extends T> RegistryDataContext<T, U1, P> cast() {
        return new RegistryDataContext<>(modid, provider, id, (U1) object);
    }
}
