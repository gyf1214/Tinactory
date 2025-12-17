package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPortNotifier;

import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedPort implements IPortNotifier {
    private final Set<Runnable> updateListeners = new HashSet<>();
    protected final Runnable combinedListener = this::invokeUpdate;

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
