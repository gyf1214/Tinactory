package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFluidStackHandler extends IFluidCollection {
    int getTanks();

    IFluidTank getTank(int index);

    @Override
    default boolean acceptInput(FluidStack stack) {
        var size = this.getTanks();
        for (var i = 0; i < size; i++) {
            if (this.getTank(i).isFluidValid(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean acceptOutput() {
        return true;
    }

    @Override
    default int fill(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty()) {
            return 0;
        }
        var action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        var stack = fluid.copy();
        var size = this.getTanks();
        for (var i = 0; i < size; i++) {
            var tank = this.getTank(i);
            if (!tank.getFluid().isEmpty() && tank.getFluid().isFluidEqual(stack)) {
                stack.shrink(tank.fill(stack, action));
                if (stack.isEmpty()) {
                    return fluid.getAmount();
                }
            }
        }
        for (var i = 0; i < size; i++) {
            var tank = this.getTank(i);
            if (tank.getFluid().isEmpty()) {
                stack.shrink(tank.fill(stack, action));
                if (stack.isEmpty()) {
                    return fluid.getAmount();
                }
            }
        }
        return fluid.getAmount() - stack.getAmount();
    }

    @Override
    default FluidStack drain(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        var size = this.getTanks();
        var stack = FluidStack.EMPTY;
        var amount = fluid.getAmount();
        for (var i = 0; i < size; i++) {
            if (amount <= 0) {
                break;
            }
            var tank = this.getTank(i);
            if (tank.getFluid().isFluidEqual(fluid)) {
                var stack1 = tank.drain(amount, action);
                if (stack.isEmpty()) {
                    stack = stack1;
                } else if (stack.isFluidEqual(stack1)) {
                    stack.grow(stack1.getAmount());
                } else {
                    // don't know what to do actually, can only destroy the extracted item
                    continue;
                }
                amount -= stack1.getAmount();
            }
        }
        return stack;
    }

    @Override
    default int getFluidAmount(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return 0;
        }
        var size = this.getTanks();
        var ret = 0;
        for (var i = 0; i < size; i++) {
            var stack = this.getTank(i).getFluid();
            if (stack.isFluidEqual(fluid)) {
                ret += stack.getAmount();
            }
        }
        return ret;
    }

    @Override
    default Collection<FluidStack> getAllFluids() {
        Map<FluidTypeWrapper, FluidStack> allFluids = new HashMap<>();

        var size = this.getTanks();
        for (var i = 0; i < size; i++) {
            var stack = this.getTank(i).getFluid();
            if (stack.isEmpty()) {
                continue;
            }
            var wrapper = new FluidTypeWrapper(stack);
            var existingStack = allFluids.get(wrapper);
            if (existingStack != null) {
                existingStack.grow(stack.getAmount());
            } else {
                allFluids.put(wrapper, stack.copy());
            }
        }

        // clean reference to the original itemStack
        return allFluids.values().stream().toList();
    }
}
