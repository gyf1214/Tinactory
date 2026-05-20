package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftCpuSyncPacket implements IPacket {
    private final List<CpuStatusEntry> entries = new ArrayList<>();

    public AutocraftCpuSyncPacket() {}

    public AutocraftCpuSyncPacket(List<CpuStatusEntry> entries) {
        this.entries.addAll(entries);
    }

    public List<CpuStatusEntry> entries() {
        return List.copyOf(entries);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(entries, (buf1, entry) -> {
            buf1.writeUUID(entry.cpuId());
            buf1.writeEnum(entry.state());
            buf1.writeCollection(entry.targets(), (buf2, amount) -> {
                buf2.writeNbt(encodeIngredientKey(amount.key()));
                buf2.writeLong(amount.amount());
            });
            buf1.writeInt(entry.completedSteps());
            buf1.writeInt(entry.totalSteps());
            buf1.writeEnum(entry.error());
        });
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        entries.clear();
        entries.addAll(buf.readList(buf1 -> new CpuStatusEntry(
            buf1.readUUID(),
            buf1.readEnum(JobState.class),
            buf1.readList(buf2 -> {
                var key = decodeIngredientKey(buf2.readNbt());
                var amount = buf2.readLong();
                return new CraftAmount(key, amount);
            }),
            buf1.readInt(),
            buf1.readInt(),
            buf1.readEnum(ExecutionError.class))));
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
        tag.put("value", CodecHelper.encodeTag(StackHelper.KEY_CODEC, key));
        return tag;
    }

    private IStackKey decodeIngredientKey(CompoundTag tag) {
        return CodecHelper.parseTag(StackHelper.KEY_CODEC, tag.get("value"));
    }
}
