package org.shsts.tinactory.integration.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSyncPacket implements IPacket {
    private FluidStack fluidStack;

    public FluidSyncPacket() {}

    public FluidSyncPacket(FluidStack fluidStack) {
        this.fluidStack = fluidStack.copy();
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        StackHelper.serializeFluidStackToBuf(buf, fluidStack);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        fluidStack = StackHelper.deserializeFluidStackFromBuf(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FluidSyncPacket that)) {
            return false;
        }
        return fluidStack.isFluidStackIdentical(that.fluidStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluidStack);
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
