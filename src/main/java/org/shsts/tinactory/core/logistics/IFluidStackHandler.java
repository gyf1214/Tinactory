package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.IFluidTank;
import org.shsts.tinactory.api.logistics.IFluidCollection;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidStackHandler extends IFluidCollection {
    int getTanks();

    IFluidTank getTank(int index);
}
