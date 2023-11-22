package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperFluidTank implements IFluidTankModifiable, INBTSerializable<CompoundTag> {
    public static final WrapperFluidTank EMPTY = new WrapperFluidTank(0);

    protected final IFluidTank tank;
    protected final List<Runnable> updateListeners = new ArrayList<>();
    public boolean allowInput = true;
    public boolean allowOutput = true;

    public WrapperFluidTank(int capacity) {
        this(new FluidTank(capacity));
    }

    public WrapperFluidTank(IFluidTank tank) {
        assert tank instanceof FluidTank ||
                (tank instanceof INBTSerializable<?> && tank instanceof IFluidTankModifiable);
        this.tank = tank;
    }

    public void onUpdate(Runnable cb) {
        this.updateListeners.add(cb);
    }

    protected void invokeUpdate() {
        for (var cb : this.updateListeners) {
            cb.run();
        }
    }

    @NotNull
    @Override
    public FluidStack getFluid() {
        return this.tank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return this.tank.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return this.tank.getCapacity();
    }

    @Override
    public boolean isFluidValid(FluidStack fluid) {
        return this.tank.isFluidValid(fluid);
    }

    @Override
    public int fill(FluidStack fluid, IFluidHandler.FluidAction action) {
        var ret = this.allowInput ? this.tank.fill(fluid, action) : 0;
        if (ret > 0) {
            this.invokeUpdate();
        }
        return ret;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack fluid, IFluidHandler.FluidAction action) {
        var ret = this.allowOutput ? this.tank.drain(fluid, action) : FluidStack.EMPTY;
        if (!ret.isEmpty()) {
            this.invokeUpdate();
        }
        return ret;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        var ret = this.allowOutput ? this.tank.drain(maxDrain, action) : FluidStack.EMPTY;
        if (!ret.isEmpty()) {
            this.invokeUpdate();
        }
        return ret;
    }

    @Override
    public void setFluid(FluidStack stack) {
        if (this.tank instanceof FluidTank fluidTank) {
            fluidTank.setFluid(stack);
        } else if (this.tank instanceof IFluidTankModifiable modifiable) {
            modifiable.setFluid(stack);
        } else {
            throw new IllegalCallerException();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        if (this.tank instanceof FluidTank fluidTank) {
            var tag = new CompoundTag();
            fluidTank.writeToNBT(tag);
            return tag;
        } else if (this.tank instanceof INBTSerializable<?> serializable) {
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
        if (this.tank instanceof FluidTank fluidTank) {
            fluidTank.readFromNBT(tag);
        } else if (this.tank instanceof INBTSerializable<?> serializable) {
            deserializeNBT(serializable, tag);
        } else {
            throw new IllegalCallerException();
        }
    }
}
