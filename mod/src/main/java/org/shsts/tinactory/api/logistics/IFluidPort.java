package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidPort extends IPort, IPortFilter<FluidStack> {
    @Override
    default PortType type() {
        return PortType.FLUID;
    }

    boolean acceptInput(FluidStack stack);

    /**
     * Returns the amount of fluid not inserted. The passed and returned FluidStack should be safely modified.
     */
    FluidStack insert(FluidStack fluid, boolean simulate);

    /**
     * Returns the fluid taken. The passed and returned FluidStack can be safely modified.
     */
    FluidStack extract(FluidStack fluid, boolean simulate);

    /**
     * Returns the fluid taken. The returned FluidStack can be safely modified.
     */
    FluidStack extract(int limit, boolean simulate);

    int getStorageAmount(FluidStack fluid);

    /**
     * DO NOT change the returned FluidStack.
     */
    Collection<FluidStack> getAllStorages();

    @Override
    default void setFilters(List<? extends Predicate<FluidStack>> filters) {}

    @Override
    default void resetFilters() {}
}
