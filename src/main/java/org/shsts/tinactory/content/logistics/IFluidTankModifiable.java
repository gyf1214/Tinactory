package org.shsts.tinactory.content.logistics;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidTankModifiable extends IFluidTank {
    void setFluid(FluidStack stack);
}
