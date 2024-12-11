package org.shsts.tinactory.datagen.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.shsts.tinactory.core.builder.Builder;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinycorelib.datagen.api.IDataGen;

import static org.shsts.tinactory.datagen.DataGen._DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DataBuilder<P, S extends DataBuilder<P, S>> extends Builder<Unit, P, S> {
    public final ResourceLocation loc;
    protected final DataGen xDataGen;
    protected final IDataGen dataGen;

    protected DataBuilder(IDataGen dataGen, P parent, String id) {
        this(dataGen, parent, new ResourceLocation(dataGen.modid(), id));
    }

    protected DataBuilder(IDataGen dataGen, P parent, ResourceLocation loc) {
        super(parent);
        this.xDataGen = _DATA_GEN;
        this.dataGen = dataGen;
        this.loc = loc;
        onBuild(this::register);
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
