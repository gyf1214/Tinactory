package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
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
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(items, (buf1, stack) ->
            StackHelper.serializeStackToBuf((RegistryFriendlyByteBuf) buf1, stack));
        buf.writeCollection(fluids, (buf1, stack) ->
            StackHelper.serializeFluidStackToBuf((RegistryFriendlyByteBuf) buf1, stack));
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        items = buf.readList(buf1 ->
            StackHelper.deserializeStackFromBuf((RegistryFriendlyByteBuf) buf1));
        fluids = buf.readList(buf1 ->
            StackHelper.deserializeFluidStackFromBuf((RegistryFriendlyByteBuf) buf1));
    }
}
