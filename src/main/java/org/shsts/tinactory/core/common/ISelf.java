package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISelf<S extends ISelf<S>> {
    @SuppressWarnings("unchecked")
    default S self() {
        return (S) this;
    }
}
