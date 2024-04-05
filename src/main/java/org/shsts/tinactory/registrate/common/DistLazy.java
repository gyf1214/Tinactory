package org.shsts.tinactory.registrate.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
@ParametersAreNonnullByDefault
public interface DistLazy<T> extends Supplier<Supplier<T>> {
    default T getValue() {
        return get().get();
    }

    default void runOnDist(Dist dist, Supplier<Consumer<T>> cons) {
        DistExecutor.unsafeRunWhenOn(dist, () -> () -> cons.get().accept(getValue()));
    }
}
