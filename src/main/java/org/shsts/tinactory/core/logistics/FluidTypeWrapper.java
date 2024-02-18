package org.shsts.tinactory.core.logistics;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public record FluidTypeWrapper(@Nonnull FluidStack stack) {
    @Override
    public String toString() {
        return "FluidTypeWrapper{%s}".formatted(this.stack.getFluid());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof FluidTypeWrapper o && this.isFluidEqual(o.stack));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stack.getFluid(), this.stack.getTag());
    }

    public boolean isFluidEqual(@Nonnull FluidStack other) {
        return this.stack.isFluidEqual(other);
    }
}
