package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FluidContentWrapper(FluidStack fluidStack) implements ILogisticsContentWrapper {
    @Override
    public int getCount() {
        return this.fluidStack.getAmount();
    }

    @Override
    public boolean isEmpty() {
        return this.fluidStack.isEmpty();
    }

    @Override
    public void grow(int amount) {
        this.fluidStack.grow(amount);
    }

    @Override
    public void shrink(int amount) {
        this.fluidStack.shrink(amount);
    }

    @Override
    public ILogisticsContentWrapper extractFrom(IPort port, boolean simulate) {
        var stack = port.asFluid().drain(this.fluidStack, simulate);
        return new FluidContentWrapper(stack);
    }

    @Override
    public ILogisticsContentWrapper insertInto(IPort port, boolean simulate) {
        var filled = port.asFluid().fill(this.fluidStack, simulate);
        var stack = this.fluidStack.copy();
        stack.shrink(filled);
        return new FluidContentWrapper(stack);
    }

    @Override
    public ILogisticsTypeWrapper getType() {
        return new FluidTypeWrapper(this.fluidStack);
    }

    @Override
    public ILogisticsContentWrapper copyWithAmount(int amount) {
        var stack = this.fluidStack.copy();
        stack.setAmount(amount);
        return new FluidContentWrapper(stack);
    }
}
