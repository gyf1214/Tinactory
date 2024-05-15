package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Event<A> extends ForgeRegistryEntry<Event<?>> {
    @SuppressWarnings("unchecked")
    public void invoke(Consumer<?> handler, A arg) {
        ((Consumer<A>) handler).accept(arg);
    }
}
