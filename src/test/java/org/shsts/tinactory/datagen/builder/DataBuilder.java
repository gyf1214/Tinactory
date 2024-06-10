package org.shsts.tinactory.datagen.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.datagen.DataGen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DataBuilder<U, P, S extends DataBuilder<U, P, S>> extends BuilderBase<U, P, S> {
    public final ResourceLocation loc;
    protected final DataGen dataGen;

    public DataBuilder(DataGen dataGen, P parent, String id) {
        super(parent);
        this.dataGen = dataGen;
        this.loc = new ResourceLocation(dataGen.modid, id);
        onBuild.add(this::register);
    }

    public DataBuilder(DataGen dataGen, P parent, ResourceLocation loc) {
        super(parent);
        this.dataGen = dataGen;
        this.loc = loc;
        onBuild.add(this::register);
    }

    protected abstract void register();

    public ResourceLocation buildLoc() {
        build();
        return loc;
    }
}
