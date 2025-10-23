package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Arrays;

import static org.shsts.tinactory.content.AllCapabilities.MENU_FLUID_HANDLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTank extends ElectricStorage implements INBTSerializable<CompoundTag> {
    public static final String ID = "machine/tank";

    private final int size;
    private final WrapperFluidTank[] tanks;
    private final CombinedFluidTank port;
    private final FluidStack[] filters;
    private final LazyOptional<IFluidStackHandler> fluidHandlerCap;

    private class VoidableFluidTank extends WrapperFluidTank {
        public VoidableFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int fill(FluidStack fluid, IFluidHandler.FluidAction action) {
            if (!isFluidValid(fluid)) {
                return 0;
            }
            var amount = fluid.getAmount();
            var ret = super.fill(fluid, action);
            return isVoid() ? amount : ret;
        }
    }

    public ElectricTank(BlockEntity blockEntity, Layout layout, int slotSize, double power) {
        super(blockEntity, layout, power);
        this.size = layout.slots.size();
        this.tanks = new WrapperFluidTank[size];
        for (var i = 0; i < size; i++) {
            var slot = i;
            tanks[i] = new VoidableFluidTank(slotSize);
            tanks[i].onUpdate(this::onSlotChange);
            tanks[i].setFilter(stack -> allowFluidInTank(slot, stack));
        }
        this.port = new CombinedFluidTank(tanks);
        this.filters = new FluidStack[size];

        this.fluidHandlerCap = LazyOptional.of(() -> port);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        Layout layout, int slotSize, double power) {
        return $ -> $.capability(ID, be -> new ElectricTank(be, layout, slotSize, power));
    }

    private boolean allowFluidInTank(int slot, FluidStack stack) {
        if (filters[slot] != null) {
            return filters[slot].isFluidEqual(stack);
        }
        var stack1 = tanks[slot].getFluid();
        return (stack1.isEmpty() && isUnlocked()) || stack.isFluidEqual(stack1);
    }

    public void setFilter(int index, FluidStack stack) {
        filters[index] = stack.copy();
    }

    public void resetFilter(int index) {
        filters[index] = null;
    }

    public FluidStack getFilter(int index) {
        return filters[index] == null ? FluidStack.EMPTY : filters[index];
    }

    @Override
    protected void onMachineConfig() {
        machine.network().ifPresent(network -> registerPort(network, port));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == MENU_FLUID_HANDLER.get()) {
            return fluidHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("tanks", port.serializeNBT());
        var tag1 = new ListTag();
        for (var i = 0; i < size; i++) {
            if (filters[i] != null) {
                var tag2 = new CompoundTag();
                filters[i].writeToNBT(tag2);
                tag2.putInt("Slot", i);
                tag1.add(tag2);
            }
        }
        tag.put("filters", tag1);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        port.deserializeNBT(tag.getCompound("tanks"));
        var tag1 = tag.getList("filters", Tag.TAG_COMPOUND);
        Arrays.fill(filters, null);
        for (var tag2 : tag1) {
            var tag3 = (CompoundTag) tag2;
            var slot = tag3.getInt("Slot");
            var stack = FluidStack.loadFluidStackFromNBT(tag3);
            filters[slot] = stack;
        }
    }
}
