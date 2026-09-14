package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageSyncPacket implements IPacket {
    private List<StorageEntry> entries;

    public StorageSyncPacket(List<StorageEntry> entries) {
        this.entries = entries;
    }

    public StorageSyncPacket() {}

    public List<StorageEntry> entries() {
        return entries;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, entries, StorageSyncPacket::serializeEntry);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        entries = CodecHelper.parseListFromBuf(buf, StorageSyncPacket::deserializeEntry);
    }

    private static void serializeEntry(RegistryFriendlyByteBuf buf, StorageEntry entry) {
        StackHelper.KEY_STREAM_CODEC.encode(buf, entry.key());
        buf.writeVarLong(entry.amount());
    }

    private static StorageEntry deserializeEntry(RegistryFriendlyByteBuf buf) {
        return new StorageEntry(StackHelper.KEY_STREAM_CODEC.decode(buf), buf.readVarLong());
    }
}
