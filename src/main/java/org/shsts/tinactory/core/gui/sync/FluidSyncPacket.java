package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSyncPacket extends MenuSyncPacket {
    private FluidStack fluidStack = FluidStack.EMPTY;

    public FluidSyncPacket() {}

    public FluidSyncPacket(int containerId, int index, FluidStack fluidStack) {
        super(containerId, index);
        this.fluidStack = fluidStack.copy();
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        fluidStack.writeToPacket(buf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        fluidStack = FluidStack.readFromPacket(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidSyncPacket that)) return false;
        if (!super.equals(o)) return false;
        return fluidStack.isFluidStackIdentical(that.fluidStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fluidStack);
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
