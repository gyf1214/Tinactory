package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidTank implements IFluidStackHandler, INBTSerializable<CompoundTag> {
    private final WrapperFluidTank[] tanks;

    public CombinedFluidTank(WrapperFluidTank... tanks) {
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Override
    public IFluidTank getTank(int index) {
        if (index < 0 || index >= tanks.length) {
            return WrapperFluidTank.EMPTY;
        }
        return tanks[index];
    }

    @Override
    public CompoundTag serializeNBT() {
        var size = tanks.length;
        var tankTags = new ListTag();
        for (var i = 0; i < size; i++) {
            var tank = tanks[i];
            if (!tank.getFluid().isEmpty()) {
                var fluidTag = tank.serializeNBT();
                fluidTag.putInt("Tank", i);
                tankTags.add(fluidTag);
            }
        }
        var tag = new CompoundTag();
        tag.putInt("Size", size);
        tag.put("Tanks", tankTags);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var tank : tanks) {
            tank.setFluid(FluidStack.EMPTY);
        }
        var listTag = tag.getList("Tanks", Tag.TAG_COMPOUND);
        for (var i = 0; i < listTag.size(); i++) {
            var tankTag = listTag.getCompound(i);
            var index = tankTag.getInt("Tank");
            if (index >= 0 && index < tanks.length) {
                tanks[index].deserializeNBT(tankTag);
            }
        }
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        return Arrays.stream(tanks).anyMatch(tank -> tank.isFluidValid(stack));
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public int fill(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty()) {
            return 0;
        }
        var action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        var stack = fluid.copy();
        for (var tank : tanks) {
            if (!tank.getFluid().isEmpty() && tank.getFluid().isFluidEqual(stack)) {
                stack.shrink(tank.fill(stack, action));
                if (stack.isEmpty()) {
                    return fluid.getAmount();
                }
            }
        }
        for (var tank : tanks) {
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
    public FluidStack drain(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        var stack = FluidStack.EMPTY;
        var amount = fluid.getAmount();
        for (var tank : tanks) {
            if (amount <= 0) {
                break;
            }
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
    public int getFluidAmount(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return 0;
        }
        var ret = 0;
        for (var tank : tanks) {
            var stack = tank.getFluid();
            if (stack.isFluidEqual(fluid)) {
                ret += stack.getAmount();
            }
        }
        return ret;
    }

    @Override
    public Collection<FluidStack> getAllFluids() {
        Map<FluidTypeWrapper, FluidStack> allFluids = new HashMap<>();

        for (var tank : tanks) {
            var stack = tank.getFluid();
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
