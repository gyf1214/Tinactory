package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperFluidTank implements IFluidTankModifiable, INBTSerializable<CompoundTag> {
    public static final WrapperFluidTank EMPTY = new WrapperFluidTank(0);

    private final IFluidTank tank;
    private final List<Runnable> updateListeners = new ArrayList<>();
    public boolean allowInput = true;
    public boolean allowOutput = true;
    public Predicate<FluidStack> filter = $ -> true;

    public WrapperFluidTank(int capacity) {
        this(new FluidTank(capacity));
    }

    public WrapperFluidTank(IFluidTank tank) {
        assert tank instanceof FluidTank ||
                (tank instanceof INBTSerializable<?> && tank instanceof IFluidTankModifiable);
        this.tank = tank;
    }

    public void onUpdate(Runnable cb) {
        updateListeners.add(cb);
    }

    public void resetFilter() {
        filter = $ -> true;
    }

    protected void invokeUpdate() {
        for (var cb : updateListeners) {
            cb.run();
        }
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        return tank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public boolean isFluidValid(FluidStack fluid) {
        return allowInput && filter.test(fluid) && tank.isFluidValid(fluid);
    }

    @Override
    public int fill(FluidStack fluid, IFluidHandler.FluidAction action) {
        var ret = isFluidValid(fluid) ? tank.fill(fluid, action) : 0;
        if (ret > 0) {
            invokeUpdate();
        }
        return ret;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack fluid, IFluidHandler.FluidAction action) {
        var ret = allowOutput ? tank.drain(fluid, action) : FluidStack.EMPTY;
        if (!ret.isEmpty()) {
            invokeUpdate();
        }
        return ret;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        var ret = allowOutput ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
        if (!ret.isEmpty()) {
            invokeUpdate();
        }
        return ret;
    }

    @Override
    public void setFluid(FluidStack stack) {
        if (tank instanceof FluidTank fluidTank) {
            fluidTank.setFluid(stack);
        } else if (tank instanceof IFluidTankModifiable modifiable) {
            modifiable.setFluid(stack);
        } else {
            throw new IllegalCallerException();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        if (tank instanceof FluidTank fluidTank) {
            var tag = new CompoundTag();
            fluidTank.writeToNBT(tag);
            return tag;
        } else if (tank instanceof INBTSerializable<?> serializable) {
            return (CompoundTag) serializable.serializeNBT();
        } else {
            throw new IllegalCallerException();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T1 extends Tag, T2 extends Tag>
    void deserializeNBT(INBTSerializable<T1> serializable, T2 tag) {
        serializable.deserializeNBT((T1) tag);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tank instanceof FluidTank fluidTank) {
            fluidTank.readFromNBT(tag);
        } else if (tank instanceof INBTSerializable<?> serializable) {
            deserializeNBT(serializable, tag);
        } else {
            throw new IllegalCallerException();
        }
    }
}
