package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FluidContentWrapper(FluidStack fluidStack) implements ILogisticsContentWrapper {
    @Override
    public int getCount() {
        return fluidStack.getAmount();
    }

    @Override
    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }

    @Override
    public void grow(int amount) {
        fluidStack.grow(amount);
    }

    @Override
    public void shrink(int amount) {
        fluidStack.shrink(amount);
    }

    @Override
    public ILogisticsContentWrapper extractFrom(IPort port, boolean simulate) {
        var stack = port.asFluid().drain(fluidStack, simulate);
        return new FluidContentWrapper(stack);
    }

    @Override
    public ILogisticsContentWrapper insertInto(IPort port, boolean simulate) {
        var filled = port.asFluid().fill(fluidStack, simulate);
        var stack = fluidStack.copy();
        stack.shrink(filled);
        return new FluidContentWrapper(stack);
    }

    @Override
    public ILogisticsTypeWrapper getType() {
        return new FluidTypeWrapper(fluidStack);
    }

    @Override
    public ILogisticsContentWrapper copyWithAmount(int amount) {
        var stack = fluidStack.copy();
        stack.setAmount(amount);
        return new FluidContentWrapper(stack);
    }
}
