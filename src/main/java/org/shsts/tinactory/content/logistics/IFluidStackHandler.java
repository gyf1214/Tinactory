package org.shsts.tinactory.content.logistics;

import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IFluidStackHandler {
    int getTanks();

    IFluidHandler getTank(int index);
}
