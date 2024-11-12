package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidCollection extends IPort {
    @Override
    default PortType type() {
        return PortType.FLUID;
    }

    boolean acceptInput(FluidStack stack);

    boolean acceptOutput();

    /**
     * returns the amount of fluid inserted
     */
    int fill(FluidStack fluid, boolean simulate);

    /**
     * returns the amount of fluid taken
     */
    FluidStack drain(FluidStack fluid, boolean simulate);

    int getFluidAmount(FluidStack fluid);

    /**
     * DO NOT change the returned ItemStack.
     */
    Collection<FluidStack> getAllFluids();

    void setFluidFilter(List<? extends Predicate<FluidStack>> filters);

    void resetFluidFilter();
}
