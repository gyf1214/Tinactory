package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.shsts.tinactory.core.common.CapabilityProvider;

import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DigitalStorage extends CapabilityProvider implements IPortNotifier {
    protected final int bytesLimit;
    protected int bytesRemaining;
    private final Set<Runnable> updateListeners = new HashSet<>();

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

    @Override
    public void onUpdate(Runnable listener) {
        updateListeners.add(listener);
    }

    @Override
    public void unregisterListener(Runnable listener) {
        updateListeners.remove(listener);
    }

    protected void invokeUpdate() {
        for (var cb : updateListeners) {
            cb.run();
        }
    }
}
