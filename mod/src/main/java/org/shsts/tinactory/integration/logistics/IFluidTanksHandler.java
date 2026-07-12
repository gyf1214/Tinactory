package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * This interface is intended to allow manipulating individual slots for a fluidHandler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidTanksHandler extends IFluidHandler {
    IFluidTank getTank(int index);
}
