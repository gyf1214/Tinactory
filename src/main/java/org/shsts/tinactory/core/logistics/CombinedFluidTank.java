package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidTank implements IFluidStackHandler, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final WrapperFluidTank[] tanks;
    private final boolean acceptOutput;

    public CombinedFluidTank(WrapperFluidTank... tanks) {
        this(true, tanks);
    }

    public CombinedFluidTank(boolean acceptOutput, WrapperFluidTank... tanks) {
        this.tanks = tanks;
        this.acceptOutput = acceptOutput;
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
    public boolean isEmpty() {
        for (var tank : tanks) {
            if (!tank.getFluid().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        return Arrays.stream(tanks).anyMatch(tank -> tank.isFluidValid(stack));
    }

    @Override
    public boolean acceptOutput() {
        return acceptOutput;
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
        if (fluid.isEmpty() || !acceptOutput()) {
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
                    // don't know what to do actually, can only destroy the extracted fluid
                    LOGGER.warn("{}: Extracted fluid {} cannot stack with required fluid {}",
                        this, stack1, stack);
                    continue;
                }
                amount -= stack1.getAmount();
            }
        }
        return stack;
    }

    @Override
    public FluidStack drain(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput()) {
            return FluidStack.EMPTY;
        }
        for (var tank : tanks) {
            var tankFluid = tank.getFluid();
            if (!tankFluid.isEmpty()) {
                var fluid = tankFluid.copy();
                fluid.setAmount(limit);
                return drain(fluid, simulate);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getFluidAmount(FluidStack fluid) {
        if (fluid.isEmpty() || !acceptOutput()) {
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
        if (!acceptOutput()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tanks)
            .map(WrapperFluidTank::getFluid)
            .filter(f -> !f.isEmpty())
            .toList();
    }

    @Override
    public void setFluidFilter(List<? extends Predicate<FluidStack>> filters) {
        for (var i = 0; i < tanks.length; i++) {
            if (i < filters.size()) {
                tanks[i].filter = filters.get(i);
            } else {
                tanks[i].filter = $ -> false;
            }
        }
    }

    @Override
    public void resetFluidFilter() {
        for (var tank : tanks) {
            tank.resetFilter();
        }
    }
}
