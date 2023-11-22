package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidTank implements IFluidStackHandler, INBTSerializable<CompoundTag> {
    protected final WrapperFluidTank[] tanks;

    public CombinedFluidTank(WrapperFluidTank... tanks) {
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return this.tanks.length;
    }

    @Override
    public IFluidTank getTank(int index) {
        if (index < 0 || index >= this.tanks.length) {
            return WrapperFluidTank.EMPTY;
        }
        return this.tanks[index];
    }

    @Override
    public CompoundTag serializeNBT() {
        var size = this.tanks.length;
        var tanks = new ListTag();
        for (var i = 0; i < size; i++) {
            var tank = this.tanks[i];
            if (!tank.getFluid().isEmpty()) {
                var fluidTag = tank.serializeNBT();
                fluidTag.putInt("Tank", i);
                tanks.add(fluidTag);
            }
        }
        var tag = new CompoundTag();
        tag.putInt("Size", size);
        tag.put("Tanks", tanks);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var tank : this.tanks) {
            tank.setFluid(FluidStack.EMPTY);
        }
        var listTag = tag.getList("Tanks", Tag.TAG_COMPOUND);
        for (var i = 0; i < listTag.size(); i++) {
            var tankTag = listTag.getCompound(i);
            var index = tankTag.getInt("Tank");
            if (index >= 0 && index < this.tanks.length) {
                this.tanks[index].deserializeNBT(tankTag);
            }
        }
    }
}
