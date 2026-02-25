package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalCpuSyncSlot implements IPacket {
    private final List<UUID> availableCpus = new ArrayList<>();

    public AutocraftTerminalCpuSyncSlot() {}

    public AutocraftTerminalCpuSyncSlot(List<UUID> availableCpus) {
        this.availableCpus.addAll(availableCpus);
    }

    public List<UUID> availableCpus() {
        return List.copyOf(availableCpus);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(availableCpus, FriendlyByteBuf::writeUUID);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        availableCpus.clear();
        availableCpus.addAll(buf.readList(FriendlyByteBuf::readUUID));
    }
}
