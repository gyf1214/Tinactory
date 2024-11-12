package org.shsts.tinactory.registrate.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.registrate.Registrate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Builder<U, P, S extends Builder<U, P, S>> extends BuilderBase<U, P, S> {
    public final String id;
    public final ResourceLocation loc;

    protected final Registrate registrate;

    public Builder(Registrate registrate, P parent, String id) {
        super(parent);
        this.registrate = registrate;
        this.id = id;
        this.loc = new ResourceLocation(registrate.modid, id);
    }

    public Builder(Registrate registrate, P parent, ResourceLocation loc) {
        super(parent);
        this.registrate = registrate;
        this.id = loc.getPath();
        this.loc = loc;
    }
}
