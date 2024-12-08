package org.shsts.tinactory.core.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChestItemSyncPacket implements IPacket {
    private ItemStack stack;
    @Nullable
    private ItemStack filter;

    public ChestItemSyncPacket() {}

    public ChestItemSyncPacket(ItemStack stack, @Nullable ItemStack filter) {
        this.stack = stack;
        this.filter = filter;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        StackHelper.serializeStackToBuf(stack, buf);
        buf.writeBoolean(filter != null);
        if (filter != null) {
            buf.writeItem(filter);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        stack = StackHelper.deserializeStackFromBuf(buf);
        var hasFilter = buf.readBoolean();
        filter = hasFilter ? buf.readItem() : null;
    }

    private boolean filterEqual(ChestItemSyncPacket that) {
        if (filter == null || that.filter == null) {
            return filter == that.filter;
        }
        return StackHelper.itemStackEqual(filter, that.filter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChestItemSyncPacket that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return StackHelper.itemStackEqual(stack, that.stack) &&
            filterEqual(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stack, filter);
    }

    public ItemStack getStack() {
        return stack;
    }

    public Optional<ItemStack> getFilter() {
        return Optional.ofNullable(filter);
    }
}
