package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidTank implements IFluidStackHandler {
    protected final WrapperFluidTank[] tanks;

    public CombinedFluidTank(WrapperFluidTank... tanks) {
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return this.tanks.length;
    }

    @Override
    public IFluidHandler getTank(int index) {
        if (index < 0 || index >= this.tanks.length) {
            return EmptyFluidHandler.INSTANCE;
        }
        return this.tanks[index];
    }
}
