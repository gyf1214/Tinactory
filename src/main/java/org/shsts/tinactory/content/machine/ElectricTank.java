package org.shsts.tinactory.content.machine;

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
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Arrays;

import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTank extends ElectricStorage implements INBTSerializable<CompoundTag> {
    private static final String ID = "machine/tank";

    private final int size;
    private final WrapperFluidTank[] innerTanks;
    private final WrapperFluidTank[] externalTanks;
    private final CombinedFluidTank innerPort;
    private final CombinedFluidTank externalPort;
    private final FluidStack[] filters;
    private final LazyOptional<IFluidStackHandler> fluidHandlerCap;

    public ElectricTank(BlockEntity blockEntity, Layout layout) {
        super(blockEntity, layout);
        this.size = layout.slots.size();
        var capacity = TinactoryConfig.INSTANCE.tankSize.get();
        this.innerTanks = new WrapperFluidTank[size];
        this.externalTanks = new WrapperFluidTank[size];
        for (var i = 0; i < size; i++) {
            var slot = i;
            innerTanks[i] = new WrapperFluidTank(capacity);
            innerTanks[i].onUpdate(this::onSlotChange);
            innerTanks[i].filter = stack -> allowFluidInTank(slot, stack);
            externalTanks[i] = new WrapperFluidTank(innerTanks[i]);
        }
        this.innerPort = new CombinedFluidTank(innerTanks);
        this.externalPort = new CombinedFluidTank(externalTanks);
        this.filters = new FluidStack[size];

        this.fluidHandlerCap = LazyOptional.of(() -> innerPort);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout) {
        return $ -> $.capability(ID, be -> new ElectricTank(be, layout));
    }

    private boolean allowFluidInTank(int slot, FluidStack stack) {
        if (filters[slot] != null) {
            return filters[slot].isFluidEqual(stack);
        }
        var stack1 = innerTanks[slot].getFluid();
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
        var allowInput = allowInput();
        var allowOutput = allowOutput();
        for (var i = 0; i < size; i++) {
            externalTanks[i].allowInput = allowInput;
            externalTanks[i].allowOutput = allowOutput;
        }
        machine.network().ifPresent(network -> registerPort(network, externalPort));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == FLUID_STACK_HANDLER.get()) {
            return fluidHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("tanks", innerPort.serializeNBT());
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
        innerPort.deserializeNBT(tag.getCompound("tanks"));
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
