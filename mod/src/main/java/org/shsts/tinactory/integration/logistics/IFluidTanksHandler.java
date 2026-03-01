package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * This interface is intended to allow manipulating individual slots for a fluidHandler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidTanksHandler extends IFluidHandler {
    IFluidTank getTank(int index);
}
