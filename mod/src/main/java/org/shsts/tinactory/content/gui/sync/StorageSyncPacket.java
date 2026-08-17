package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageSyncPacket implements IPacket {
    public record Entry(IStackKey key, int amount, boolean isFilter) {
        public static void serialize(RegistryFriendlyByteBuf buf, Entry entry) {
            StackHelper.KEY_STREAM_CODEC.encode(buf, entry.key());
            buf.writeVarInt(entry.amount());
            buf.writeBoolean(entry.isFilter());
        }

        public static Entry deserialize(RegistryFriendlyByteBuf buf) {
            return new Entry(StackHelper.KEY_STREAM_CODEC.decode(buf),
                buf.readVarInt(),
                buf.readBoolean());
        }
    }

    private List<Entry> entries;

    public StorageSyncPacket(List<Entry> entries) {
        this.entries = entries;
    }

    public StorageSyncPacket(Collection<ItemStack> items, Collection<FluidStack> fluids) {
        this.entries = new ArrayList<>();
        for (var item : items) {
            entries.add(new Entry(StackHelper.ITEM_ADAPTER.keyOf(item), item.getCount(), false));
        }
        for (var fluid : fluids) {
            entries.add(new Entry(StackHelper.FLUID_ADAPTER.keyOf(fluid), fluid.getAmount(), false));
        }
    }

    public StorageSyncPacket() {}

    public List<Entry> entries() {
        return entries;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, entries, Entry::serialize);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        entries = CodecHelper.parseListFromBuf(buf, Entry::deserialize);
    }
}
