package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidCollection {
    boolean acceptInput(FluidStack stack);

    boolean acceptOutput();

    int fill(FluidStack fluid, boolean simulate);

    FluidStack drain(FluidStack fluid, boolean simulate);

    int getFluidAmount(FluidStack fluid);

    /**
     * DO NOT change the returned ItemStack.
     */
    Collection<FluidStack> getAllFluids();
}
