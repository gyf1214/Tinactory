package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IDigitalProvider {
    int bytesUsed();

    int consumeLimit(int bytes);

    default boolean canConsume(int bytes) {
        return consumeLimit(bytes) > 0;
    }

    void consume(int bytes);

    default void restore(int bytes) {
        consume(-bytes);
    }

    void reset();
}
