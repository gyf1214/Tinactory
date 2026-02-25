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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalRequestablesSyncSlot implements IPacket {
    private final List<AutocraftRequestableEntry> requestables = new ArrayList<>();

    public AutocraftTerminalRequestablesSyncSlot() {}

    public AutocraftTerminalRequestablesSyncSlot(List<AutocraftRequestableEntry> requestables) {
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
}
