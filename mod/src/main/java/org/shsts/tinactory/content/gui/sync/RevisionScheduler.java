package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.network.IPacketType;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RevisionScheduler<P extends IPacket> implements ISyncSlotScheduler<P> {
    private final IPacketType<P> type;
    private final LongSupplier revisionSupplier;
    private final Supplier<P> packetFactory;
    private long currentRevision = Long.MIN_VALUE;

    public RevisionScheduler(IPacketType<P> type, LongSupplier revisionSupplier,
        Supplier<P> packetFactory) {
        this.type = type;
        this.revisionSupplier = revisionSupplier;
        this.packetFactory = packetFactory;
    }

    @Override
    public IPacketType<P> packetType() {
        return type;
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
