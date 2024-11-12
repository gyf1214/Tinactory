package org.shsts.tinactory.datagen.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.datagen.DataGen;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DataBuilder<P, S extends DataBuilder<P, S>> extends BuilderBase<Unit, P, S> {
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

    @Override
    protected Unit createObject() {
        return Unit.INSTANCE;
    }
}
