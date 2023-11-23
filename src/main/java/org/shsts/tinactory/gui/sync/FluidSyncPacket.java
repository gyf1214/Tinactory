package org.shsts.tinactory.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSyncPacket extends ContainerSyncPacket {
    protected FluidStack fluidStack = FluidStack.EMPTY;

    public FluidSyncPacket() {}

    public FluidSyncPacket(int containerId, int index, FluidStack fluidStack) {
        super(containerId, index);
        this.fluidStack = fluidStack.copy();
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        this.fluidStack.writeToPacket(buf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        this.fluidStack = FluidStack.readFromPacket(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidSyncPacket that)) return false;
        if (!super.equals(o)) return false;
        return this.fluidStack.isFluidStackIdentical(that.fluidStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.fluidStack);
    }

    public FluidStack getFluidStack() {
        return this.fluidStack;
    }
}
