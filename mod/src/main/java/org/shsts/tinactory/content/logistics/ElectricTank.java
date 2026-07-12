package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.logistics.CombinedFluidTank;
import org.shsts.tinactory.integration.logistics.WrapperFluidTank;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Arrays;

import static net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents;
import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MENU_FLUID_HANDLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTank extends ElectricStorage implements INBTSerializable<CompoundTag> {
    public static final String ID = "machine/tank";

    private final int capacity;
    private final int size;
    private final WrapperFluidTank[] tanks;
    private final CombinedFluidTank port;
    private final FluidStack[] filters;

    private class VoidableFluidTank extends WrapperFluidTank {
        public VoidableFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public int getCapacity() {
            return isVoid() ? Integer.MAX_VALUE : super.getCapacity();
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

    public ElectricTank(BlockEntity blockEntity, Layout layout, int capacity, double power) {
        super(blockEntity, layout, power);
        this.capacity = capacity;
        this.size = layout.slots.size();
        this.tanks = new WrapperFluidTank[size];
        for (var i = 0; i < size; i++) {
            var slot = i;
            tanks[i] = new VoidableFluidTank(capacity);
            tanks[i].onUpdate(this::onSlotChange);
            tanks[i].setFilter(stack -> allowFluidInTank(slot, stack));
        }
        this.port = new CombinedFluidTank(tanks);
        this.filters = new FluidStack[size];
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        Layout layout, int slotSize, double power) {
        return $ -> $.container(ID, be -> new ElectricTank(be, layout, slotSize, power));
    }

    private boolean allowFluidInTank(int slot, FluidStack stack) {
        if (filters[slot] != null) {
            return isSameFluidSameComponents(filters[slot], stack);
        }
        var stack1 = tanks[slot].getFluid();
        return (stack1.isEmpty() && isUnlocked()) || isSameFluidSameComponents(stack, stack1);
    }

    public void setFilter(int index, FluidStack stack) {
        filters[index] = stack.copy();
        onSlotChange();
    }

    public void resetFilter(int index) {
        filters[index] = null;
        onSlotChange();
    }

    public FluidStack getFilter(int index) {
        return filters[index] == null ? FluidStack.EMPTY : filters[index];
    }

    @Override
    protected void onMachineConfig() {
        machine().network().ifPresent(network -> registerPort(network, port));
    }

    @Override
    protected int updateSignal() {
        var totalCapacity = 0;
        var totalAmount = 0;
        for (var i = 0; i < size; i++) {
            if (filters[i] != null) {
                totalCapacity += capacity;
                totalAmount += tanks[i].getFluidAmount();
            }
        }
        return totalCapacity == 0 ? 0 : MathUtil.toSignal((double) totalAmount / totalCapacity);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        super.attachCapability(builder);
        builder.attach(FLUID_HANDLER, port);
        builder.attach(MENU_FLUID_HANDLER, port);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.put("tanks", port.serializeNBT(provider));
        var tag1 = new ListTag();
        for (var i = 0; i < size; i++) {
            if (filters[i] != null) {
                var tag2 = new CompoundTag();
                filters[i].save(provider, tag2);
                tag2.putInt("Slot", i);
                tag1.add(tag2);
            }
        }
        tag.put("filters", tag1);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        port.deserializeNBT(provider, tag.getCompound("tanks"));
        var tag1 = tag.getList("filters", Tag.TAG_COMPOUND);
        Arrays.fill(filters, null);
        for (var tag2 : tag1) {
            var tag3 = (CompoundTag) tag2;
            var slot = tag3.getInt("Slot");
            var stack = FluidStack.parseOptional(provider, tag3);
            filters[slot] = stack;
        }
    }
}
