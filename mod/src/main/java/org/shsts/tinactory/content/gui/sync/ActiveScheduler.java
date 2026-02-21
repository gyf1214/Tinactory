package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ActiveScheduler<P extends IPacket> implements ISyncSlotScheduler<P> {
    private final Supplier<P> packetFactory;
    private boolean needUpdate = true;

    public ActiveScheduler(Supplier<P> packetFactory) {
        this.packetFactory = packetFactory;
    }

    public void invokeUpdate() {
        needUpdate = true;
    }

    @Override
    public boolean shouldSend() {
        return needUpdate;
    }

    @Override
    public P createPacket() {
        needUpdate = false;
        return packetFactory.get();
    }
}
