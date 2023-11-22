package org.shsts.tinactory.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSyncPacket extends ContainerSyncPacket {
    public final FluidStack fluidStack;

    public FluidSyncPacket(int containerId, int index, FluidStack fluidStack) {
        super(containerId, index);
        this.fluidStack = fluidStack;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        this.fluidStack.writeToPacket(buf);
    }

    public static FluidSyncPacket create(FriendlyByteBuf buf) {
        var containerId = buf.readVarInt();
        var index = buf.readVarInt();
        var fluidStack = FluidStack.readFromPacket(buf);
        return new FluidSyncPacket(containerId, index, fluidStack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidSyncPacket that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(this.fluidStack, that.fluidStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.fluidStack);
    }
}
