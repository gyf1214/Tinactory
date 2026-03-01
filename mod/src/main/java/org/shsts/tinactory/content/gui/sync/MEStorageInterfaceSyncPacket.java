package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceSyncPacket implements IPacket {
    private Collection<ItemStack> items;
    private Collection<FluidStack> fluids;

    public MEStorageInterfaceSyncPacket(Collection<ItemStack> items, Collection<FluidStack> fluids) {
        this.items = items;
        this.fluids = fluids;
    }

    public MEStorageInterfaceSyncPacket() {}

    public Collection<ItemStack> items() {
        return items;
    }

    public Collection<FluidStack> fluids() {
        return fluids;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(items, StackHelper::serializeStackToBuf);
        buf.writeCollection(fluids, (buf1, stack) -> stack.writeToPacket(buf1));
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        items = buf.readList(StackHelper::deserializeStackFromBuf);
        fluids = buf.readList(FluidStack::readFromPacket);
    }
}
