package org.shsts.tinactory.integration.logistics;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

public interface IFluidTankModifiable extends IFluidTank {
    void setFluid(FluidStack stack);
}
