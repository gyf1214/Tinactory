package org.shsts.tinactory.core.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
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
    public void serializeToBuf(FriendlyByteBuf buf) {
        fluidStack.writeToPacket(buf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        fluidStack = FluidStack.readFromPacket(buf);
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
        return Objects.hash(super.hashCode(), fluidStack);
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
