package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Event<A> extends ForgeRegistryEntry<Event<?>> {
    private final Class<A> argClass;

    public A cast(Object sth) {
        return argClass.cast(sth);
    }

    public Event(Class<A> argClass) {
        this.argClass = argClass;
    }
}
