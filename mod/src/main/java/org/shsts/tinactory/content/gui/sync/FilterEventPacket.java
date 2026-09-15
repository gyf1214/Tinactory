package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.OptionalInt;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FilterEventPacket implements IPacket {
    private boolean remove;
    private int index;
    private FilterEntry filter;

    public FilterEventPacket() {}

    public FilterEventPacket(boolean remove, int index, FilterEntry filter) {
        this.remove = remove;
        this.index = index;
        this.filter = filter;
    }

    public OptionalInt remove() {
        return remove ? OptionalInt.of(index) : OptionalInt.empty();
    }

    public FilterEntry append() {
        return filter;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(remove);
        if (remove) {
            buf.writeVarInt(index);
        }
        FilterEntry.STREAM_CODEC.encode(buf, filter);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        remove = buf.readBoolean();
        if (remove) {
            index = buf.readVarInt();
        }
        filter = FilterEntry.STREAM_CODEC.decode(buf);
    }
}
