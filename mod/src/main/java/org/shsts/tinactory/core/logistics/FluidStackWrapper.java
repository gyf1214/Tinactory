package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FluidStackWrapper(FluidStack stack) implements IIngredientWrapper {
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof FluidStackWrapper wrapper &&
            stack.isFluidEqual(wrapper.stack));
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getFluid(), stack.getTag());
    }
}
