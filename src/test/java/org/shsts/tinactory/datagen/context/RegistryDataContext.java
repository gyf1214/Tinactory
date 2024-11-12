package org.shsts.tinactory.datagen.context;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

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

    @SuppressWarnings("unchecked")
    public <U1 extends T> RegistryDataContext<T, U1, P> convert() {
        return (RegistryDataContext<T, U1, P>) this;
    }
}
