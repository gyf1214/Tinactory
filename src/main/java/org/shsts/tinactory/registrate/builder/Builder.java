package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.ISelf;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Builder<U, P, S extends Builder<U, P, S>> implements ISelf<S> {
    protected final Registrate registrate;
    protected final P parent;

    protected Builder(Registrate registrate, P parent) {
        this.registrate = registrate;
        this.parent = parent;
    }

    public abstract U buildObject();

    public P build() {
        this.buildObject();
        return this.parent;
    }

    public S transform(Transformer<S> trans) {
        return trans.apply(self());
    }
}
