package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FluidTypeWrapper(@Nonnull FluidStack stack) implements ILogisticsTypeWrapper {
    @Override
    public String toString() {
        return "FluidTypeWrapper{%s}".formatted(this.stack.getFluid());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof FluidTypeWrapper o && this.stack.isFluidEqual(o.stack));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stack.getFluid(), this.stack.getTag());
    }

    @Override
    public PortType getPortType() {
        return PortType.FLUID;
    }
}
