package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableEntry;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableKey;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalRequestablesSyncPacket implements IPacket {
    private final List<AutocraftRequestableEntry> requestables = new ArrayList<>();

    public AutocraftTerminalRequestablesSyncPacket() {}

    public AutocraftTerminalRequestablesSyncPacket(List<AutocraftRequestableEntry> requestables) {
        this.requestables.addAll(requestables);
    }

    public List<AutocraftRequestableEntry> requestables() {
        return List.copyOf(requestables);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(requestables, (buf1, entry) -> {
            buf1.writeEnum(entry.key().type());
            buf1.writeUtf(entry.key().id());
            buf1.writeUtf(entry.key().nbt());
            buf1.writeLong(entry.producerCount());
        });
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        requestables.clear();
        requestables.addAll(buf.readList(buf1 -> new AutocraftRequestableEntry(
            new AutocraftRequestableKey(buf1.readEnum(CraftKey.Type.class), buf1.readUtf(), buf1.readUtf()),
            buf1.readLong())));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AutocraftTerminalRequestablesSyncPacket other)) {
            return false;
        }
        return requestables.equals(other.requestables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestables);
    }
}
