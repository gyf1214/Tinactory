package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
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
    public record CpuInfo(CpuStatusEntry status, Component name, ItemStack icon) {
        private static void serialize(FriendlyByteBuf buf, CpuInfo info) {
            serializeStatus(buf, info.status);
            buf.writeUtf(CodecHelper.encodeComponent(info.name));
            var jo = CodecHelper.encodeJson(ItemStack.CODEC, info.icon);
            buf.writeUtf(CodecHelper.jsonToStr(jo));
        }

        private static CpuInfo deserialize(FriendlyByteBuf buf) {
            return new CpuInfo(
                deserializeStatus(buf),
                CodecHelper.parseComponent(buf.readUtf()),
                CodecHelper.parseJson(ItemStack.CODEC, CodecHelper.jsonFromStr(buf.readUtf())));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CpuInfo other)) {
                return false;
            }
            return status.equals(other.status) &&
                CodecHelper.encodeComponent(name).equals(CodecHelper.encodeComponent(other.name)) &&
                ItemStack.matches(icon, other.icon);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, CodecHelper.encodeComponent(name),
                icon.getItem(), icon.getCount(), icon.getTag());
        }
    }

    private final List<CpuInfo> entries = new ArrayList<>();

    public AutocraftCpuSyncPacket() {}

    public AutocraftCpuSyncPacket(List<CpuInfo> entries) {
        this.entries.addAll(entries);
    }

    public List<CpuInfo> entries() {
        return List.copyOf(entries);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(entries, CpuInfo::serialize);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        entries.clear();
        entries.addAll(buf.readList(CpuInfo::deserialize));
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

    private static void serializeStatus(FriendlyByteBuf buf, CpuStatusEntry entry) {
        buf.writeUUID(entry.cpuId());
        buf.writeEnum(entry.state());
        buf.writeCollection(entry.targets(), (buf1, amount) -> {
            buf1.writeNbt(encodeIngredientKey(amount.key()));
            buf1.writeLong(amount.amount());
        });
        buf.writeInt(entry.completedSteps());
        buf.writeInt(entry.totalSteps());
        buf.writeEnum(entry.error());
    }

    private static CpuStatusEntry deserializeStatus(FriendlyByteBuf buf) {
        return new CpuStatusEntry(
            buf.readUUID(),
            buf.readEnum(JobState.class),
            buf.readList(buf1 -> {
                var key = decodeIngredientKey(buf1.readNbt());
                var amount = buf1.readLong();
                return new CraftAmount(key, amount);
            }),
            buf.readInt(),
            buf.readInt(),
            buf.readEnum(ExecutionError.class));
    }

    private static CompoundTag encodeIngredientKey(IStackKey key) {
        var tag = new CompoundTag();
        tag.put("value", CodecHelper.encodeTag(StackHelper.KEY_CODEC, key));
        return tag;
    }

    private static IStackKey decodeIngredientKey(CompoundTag tag) {
        return CodecHelper.parseTag(StackHelper.KEY_CODEC, tag.get("value"));
    }
}
