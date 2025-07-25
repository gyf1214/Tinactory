package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.common.CapabilityProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DigitalStorage extends CapabilityProvider {
    protected final int bytesLimit;
    protected int bytesRemaining;

    public DigitalStorage(int bytesLimit) {
        this.bytesLimit = bytesLimit;
        this.bytesRemaining = bytesLimit;
    }

    public int bytesLimit() {
        return bytesLimit;
    }

    public int bytesUsed() {
        return bytesLimit - bytesRemaining;
    }
}
