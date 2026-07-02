package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.JobState;
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
public class MECraftCpuSyncPacket implements IPacket {
    public record CpuInfo(CpuStatusEntry status, Component name, ItemStack icon) {
        private static void serialize(RegistryFriendlyByteBuf buf, CpuInfo info) {
            serializeStatus(buf, info.status);
            CodecHelper.encodeComponentToBuf(buf, info.name);
            StackHelper.serializeStackToBuf(buf, info.icon);
        }

        private static CpuInfo deserialize(RegistryFriendlyByteBuf buf) {
            return new CpuInfo(
                deserializeStatus(buf),
                CodecHelper.parseComponentFromBuf(buf),
                StackHelper.deserializeStackFromBuf(buf));
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
                name.equals(other.name) &&
                ItemStack.matches(icon, other.icon);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, name,
                ItemStack.hashItemAndComponents(icon), icon.getCount());
        }
    }

    private final List<CpuInfo> entries = new ArrayList<>();

    public MECraftCpuSyncPacket() {}

    public MECraftCpuSyncPacket(List<CpuInfo> entries) {
        this.entries.addAll(entries);
    }

    public List<CpuInfo> entries() {
        return entries;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(entries, CpuInfo::serialize);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        entries.clear();
        entries.addAll(buf.readList(CpuInfo::deserialize));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MECraftCpuSyncPacket other)) {
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
            buf1.writeNbt(CodecHelper.encodeTag(StackHelper.KEY_CODEC, amount.key()));
            buf1.writeLong(amount.amount());
        });
        buf.writeInt(entry.completedSteps());
        buf.writeInt(entry.totalSteps());
        buf.writeEnum(entry.error());
        buf.writeLong(entry.memoryLimit());
        buf.writeLong(entry.memoryUsage());
    }

    private static CpuStatusEntry deserializeStatus(FriendlyByteBuf buf) {
        return new CpuStatusEntry(
            buf.readUUID(),
            buf.readEnum(JobState.class),
            buf.readList(buf1 -> {
                var tag = CodecHelper.readRequiredNbt(buf1, "craft target key");
                var key = CodecHelper.parseTag(StackHelper.KEY_CODEC, tag);
                var amount = buf1.readLong();
                return new CraftAmount(key, amount);
            }),
            buf.readInt(),
            buf.readInt(),
            buf.readEnum(ExecutionError.class),
            buf.readLong(),
            buf.readLong());
    }
}
