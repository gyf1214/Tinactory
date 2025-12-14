package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IDigitalProvider extends IBytesProvider {
    int consumeLimit(int offset, int bytes);

    default int consumeLimit(int bytes) {
        return consumeLimit(0, bytes);
    }

    default boolean canConsume(int bytes) {
        return consumeLimit(0, bytes) > 0;
    }

    void consume(int bytes);

    default void restore(int bytes) {
        consume(-bytes);
    }

    void reset();
}
