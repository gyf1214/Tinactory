package org.shsts.tinactory.registrate.context;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DataContext<P extends DataProvider> {
    public final String modid;
    public final P provider;

    public DataContext(String modid, P provider) {
        this.modid = modid;
        this.provider = provider;
    }

    public ResourceLocation modLoc(String id) {
        return new ResourceLocation(this.modid, id);
    }
}
