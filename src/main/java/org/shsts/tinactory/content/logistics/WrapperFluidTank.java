package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperFluidTank extends FluidTank implements INBTSerializable<CompoundTag> {
    protected final List<Runnable> updateListeners = new ArrayList<>();
    public boolean allowInput = true;
    public boolean allowOutput = true;

    public WrapperFluidTank(int capacity) {
        super(capacity);
    }

    public void onUpdate(Runnable cb) {
        this.updateListeners.add(cb);
    }

    @Override
    protected void onContentsChanged() {
        for (var cb : this.updateListeners) {
            cb.run();
        }
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.allowInput ? super.fill(resource, action) : 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return this.allowOutput ? super.drain(resource, action) : FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.allowOutput ? super.drain(maxDrain, action) : FluidStack.EMPTY;
    }

    public void set(FluidStack stack) {
        this.fluid = stack;
        this.onContentsChanged();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        this.writeToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.readFromNBT(tag);
    }
}
