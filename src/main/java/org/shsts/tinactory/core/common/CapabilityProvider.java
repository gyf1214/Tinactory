package org.shsts.tinactory.core.common;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public abstract class CapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<?> myself = LazyOptional.of(() -> this);

    protected <T> LazyOptional<T> myself() {
        return myself.cast();
    }
}
