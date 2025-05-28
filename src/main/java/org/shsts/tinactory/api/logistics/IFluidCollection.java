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

    /**
     * Returns the amount of fluid inserted. The passed FluidStack can be safely modified.
     */
    int fill(FluidStack fluid, boolean simulate);

    /**
     * Returns the fluid taken. The passed and returned FluidStack can be safely modified.
     */
    FluidStack drain(FluidStack fluid, boolean simulate);

    /**
     * Returns the fluid taken. The returned FluidStack can be safely modified.
     */
    FluidStack drain(int limit, boolean simulate);

    int getFluidAmount(FluidStack fluid);

    /**
     * DO NOT change the returned ItemStack.
     */
    Collection<FluidStack> getAllFluids();

    void setFluidFilter(List<? extends Predicate<FluidStack>> filters);

    void resetFluidFilter();
}
