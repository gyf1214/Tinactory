package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.NetworkComponent;

import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class NotifierComponent extends NetworkComponent {
    private final Set<Runnable> callbacks = new HashSet<>();
    private boolean isConnecting = false;

    public NotifierComponent(ComponentType<?> type, INetwork network) {super(type, network);}

    protected void invokeUpdate() {
        if (isConnecting) {
            return;
        }
        for (var cb : callbacks) {
            cb.run();
        }
    }

    public void onUpdate(Runnable cb) {
        callbacks.add(cb);
    }

    public void unregisterCallback(Runnable cb) {
        callbacks.remove(cb);
    }

    @Override
    public void onConnect() {
        isConnecting = true;
    }

    @Override
    public void onPostConnect() {
        isConnecting = false;
        invokeUpdate();
    }

    @Override
    public void onDisconnect() {
        callbacks.clear();
    }
}
