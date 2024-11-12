package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISelf<S extends ISelf<S>> {
    @SuppressWarnings("unchecked")
    default S self() {
        return (S) this;
    }
}
