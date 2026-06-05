package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IDigitalProvider extends IBytesProvider {
    int consumeLimit(int offset, int bytes);

    default int consumeLimit(IStackKey key, int offset, int bytes) {
        return consumeLimit(offset, bytes);
    }

    default int consumeLimit(int bytes) {
        return consumeLimit(0, bytes);
    }

    default int consumeLimit(IStackKey key, int bytes) {
        return consumeLimit(key, 0, bytes);
    }

    default boolean canConsume(int bytes) {
        return consumeLimit(0, bytes) > 0;
    }

    default boolean canConsume(IStackKey key, int bytes) {
        return consumeLimit(key, 0, bytes) > 0;
    }

    void consume(int bytes);

    default void consume(IStackKey key, int bytes) {
        consume(bytes);
    }

    default void restore(int bytes) {
        consume(-bytes);
    }

    default void restore(IStackKey key, int bytes) {
        consume(key, -bytes);
    }

    void reset();
}
