package org.shsts.tinactory.core.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinycorelib.api.core.IBuilder;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SimpleBuilder<U, P, S extends IBuilder<U, P, S>> extends Builder<U, P, S> {
    protected SimpleBuilder(P parent) {
        super(parent);
        onBuild(this::buildObject);
    }
}
