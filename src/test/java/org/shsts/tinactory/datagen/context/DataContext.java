package org.shsts.tinactory.datagen.context;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DataContext<P extends DataProvider> {
    public final String modid;
    public final P provider;

    public DataContext(String modid, P provider) {
        this.modid = modid;
        this.provider = provider;
    }
}
