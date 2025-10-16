package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalProvider implements IDigitalProvider {
    private final int bytesLimit;
    private int bytesRemaining;

    public DigitalProvider(int bytesLimit) {
        this.bytesLimit = bytesLimit;
        this.bytesRemaining = bytesLimit;
    }

    @Override
    public int bytesUsed() {
        return bytesLimit - bytesRemaining;
    }

    @Override
    public int consumeLimit(int offset, int bytes) {
        return Math.max(0, (bytesRemaining - offset) / bytes);
    }

    @Override
    public void consume(int bytes) {
        bytesRemaining -= bytes;
        assert bytesRemaining >= 0 && bytesRemaining <= bytesLimit;
    }

    @Override
    public void reset() {
        bytesRemaining = bytesLimit;
    }
}
