package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftRequestablesSyncPacket implements IPacket {
    private final List<CraftKey> requestables = new ArrayList<>();

    public AutocraftRequestablesSyncPacket() {}

    public AutocraftRequestablesSyncPacket(List<CraftKey> requestables) {
        this.requestables.addAll(requestables);
    }

    public List<CraftKey> requestables() {
        return requestables;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(requestables, (buf1, entry) -> {
            buf1.writeEnum(entry.type());
            buf1.writeUtf(entry.id());
            buf1.writeUtf(entry.nbt());
        });
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        requestables.clear();
        requestables.addAll(
            buf.readList(buf1 -> new CraftKey(buf1.readEnum(CraftKey.Type.class), buf1.readUtf(), buf1.readUtf())));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AutocraftRequestablesSyncPacket other)) {
            return false;
        }
        return requestables.equals(other.requestables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestables);
    }
}
