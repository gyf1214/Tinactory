package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalProvider implements IDigitalProvider {
    private final long bytesLimit;
    private long bytesRemaining;

    public DigitalProvider(long bytesLimit) {
        this.bytesLimit = bytesLimit;
        this.bytesRemaining = bytesLimit;
    }

    @Override
    public long bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public long bytesUsed() {
        return bytesLimit - bytesRemaining;
    }

    @Override
    public int consumeLimit(int offset, int bytes) {
        var limit = Math.max(0L, (bytesRemaining - offset) / bytes);
        return limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
    }

    @Override
    public void consume(long bytes) {
        bytesRemaining -= bytes;
        assert bytesRemaining >= 0 && bytesRemaining <= bytesLimit;
    }

    @Override
    public void reset() {
        bytesRemaining = bytesLimit;
    }
}
