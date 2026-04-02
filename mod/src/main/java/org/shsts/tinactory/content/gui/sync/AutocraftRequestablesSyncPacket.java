package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.IngredientKeyCodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftRequestablesSyncPacket implements IPacket {
    private final List<IIngredientKey> requestables = new ArrayList<>();

    public AutocraftRequestablesSyncPacket() {}

    public AutocraftRequestablesSyncPacket(List<IIngredientKey> requestables) {
        this.requestables.addAll(requestables);
    }

    public List<IIngredientKey> requestables() {
        return requestables;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(requestables,
            (buf1, entry) ->
                buf1.writeNbt((CompoundTag) CodecHelper.encodeTag(IngredientKeyCodecHelper.CODEC, entry)));
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        requestables.clear();
        requestables.addAll(
            buf.readList(buf1 -> CodecHelper.parseTag(IngredientKeyCodecHelper.CODEC, buf1.readNbt())));
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
