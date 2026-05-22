package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RevisionScheduler<P extends IPacket> implements ISyncSlotScheduler<P> {
    private final LongSupplier revisionSupplier;
    private final Supplier<P> packetFactory;
    private long currentRevision = Long.MIN_VALUE;

    public RevisionScheduler(LongSupplier revisionSupplier, Supplier<P> packetFactory) {
        this.revisionSupplier = revisionSupplier;
        this.packetFactory = packetFactory;
    }

    @Override
    public boolean shouldSend() {
        return revisionSupplier.getAsLong() != currentRevision;
    }

    @Override
    public P createPacket() {
        currentRevision = revisionSupplier.getAsLong();
        return packetFactory.get();
    }
}
