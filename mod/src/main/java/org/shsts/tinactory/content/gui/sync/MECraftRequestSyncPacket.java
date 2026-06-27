package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftRequestSyncPacket implements IPacket {
    private final List<IStackKey> requestables = new ArrayList<>();

    public MECraftRequestSyncPacket() {}

    public MECraftRequestSyncPacket(List<IStackKey> requestables) {
        this.requestables.addAll(requestables);
    }

    public List<IStackKey> requestables() {
        return requestables;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(requestables,
            (buf1, entry) ->
                buf1.writeNbt((CompoundTag) CodecHelper.encodeTag(StackHelper.KEY_CODEC, entry)));
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        requestables.clear();
        requestables.addAll(
            buf.readList(buf1 -> CodecHelper.parseTag(StackHelper.KEY_CODEC,
                CodecHelper.readRequiredNbt(buf1, "requestable key"))));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MECraftRequestSyncPacket other)) {
            return false;
        }
        return requestables.equals(other.requestables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestables);
    }
}
