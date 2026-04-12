package org.shsts.tinactory.content.gui.sync;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.integration.logistics.IngredientKeyCodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftCpuSyncPacket implements IPacket {
    private final Codec<IStackKey> ingredientKeyCodec;
    private final List<CpuStatusEntry> entries = new ArrayList<>();

    public AutocraftCpuSyncPacket() {
        this(IngredientKeyCodecHelper.CODEC);
    }

    public AutocraftCpuSyncPacket(Codec<IStackKey> ingredientKeyCodec) {
        this.ingredientKeyCodec = ingredientKeyCodec;
    }

    public AutocraftCpuSyncPacket(List<CpuStatusEntry> entries) {
        this(IngredientKeyCodecHelper.CODEC, entries);
    }

    public AutocraftCpuSyncPacket(
        Codec<IStackKey> ingredientKeyCodec,
        List<CpuStatusEntry> entries) {
        this.ingredientKeyCodec = ingredientKeyCodec;
        this.entries.addAll(entries);
    }

    public List<CpuStatusEntry> entries() {
        return List.copyOf(entries);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(entries, (buf1, entry) -> {
            buf1.writeUUID(entry.cpuId());
            buf1.writeBoolean(entry.available());
            buf1.writeCollection(entry.targets(), (buf2, amount) -> {
                buf2.writeNbt(encodeIngredientKey(amount.key()));
                buf2.writeLong(amount.amount());
            });
            buf1.writeEnum(entry.state());
            buf1.writeBoolean(entry.phase() != null);
            if (entry.phase() != null) {
                buf1.writeEnum(entry.phase());
            }
            buf1.writeInt(entry.nextStepIndex());
            buf1.writeInt(entry.stepCount());
            buf1.writeEnum(entry.error());
            buf1.writeBoolean(entry.cancellable());
        });
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        entries.clear();
        entries.addAll(buf.readList(buf1 -> new CpuStatusEntry(
            buf1.readUUID(),
            buf1.readBoolean(),
            buf1.readList(buf2 -> {
                var key = decodeIngredientKey(buf2.readNbt());
                var amount = buf2.readLong();
                return new CraftAmount(key, amount);
            }),
            buf1.readEnum(JobState.class),
            buf1.readBoolean() ? buf1.readEnum(ExecutionPhase.class) : null,
            buf1.readInt(),
            buf1.readInt(),
            buf1.readEnum(ExecutionError.class),
            buf1.readBoolean())));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AutocraftCpuSyncPacket other)) {
            return false;
        }
        return entries.equals(other.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    private CompoundTag encodeIngredientKey(IStackKey key) {
        var tag = new CompoundTag();
        tag.put("value", CodecHelper.encodeTag(ingredientKeyCodec, key));
        return tag;
    }

    private IStackKey decodeIngredientKey(CompoundTag tag) {
        return CodecHelper.parseTag(ingredientKeyCodec, tag.get("value"));
    }
}
