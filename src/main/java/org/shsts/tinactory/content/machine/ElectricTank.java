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
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import java.util.Arrays;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTank extends ElectricStorage implements INBTSerializable<CompoundTag> {
    private final int size;
    private final WrapperFluidTank[] innerTanks;
    private final WrapperFluidTank[] tanks;
    private final CombinedFluidTank view;
    private final CombinedFluidTank inner;
    private final FluidStack[] filters;
    private final LazyOptional<IFluidStackHandler> fluidHandlerCap;

    public ElectricTank(BlockEntity blockEntity, Layout layout) {
        super(blockEntity);
        this.size = layout.slots.size();
        var capacity = TinactoryConfig.INSTANCE.tankSize.get();
        this.innerTanks = new WrapperFluidTank[size];
        this.tanks = new WrapperFluidTank[size];
        for (var i = 0; i < size; i++) {
            var slot = i;
            innerTanks[i] = new WrapperFluidTank(capacity);
            innerTanks[i].onUpdate(this::onSlotChange);
            innerTanks[i].filter = stack -> allowFluidInTank(slot, stack);
            tanks[i] = new WrapperFluidTank(innerTanks[i]);
        }
        this.inner = new CombinedFluidTank(innerTanks);
        this.view = new CombinedFluidTank(tanks);
        this.filters = new FluidStack[size];

        this.fluidHandlerCap = LazyOptional.of(() -> inner);
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
    protected void onConnect(Network network) {
        super.onConnect(network);
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        logistics.addStorage(view);
    }

    @Override
    protected void onMachineConfig() {
        for (var i = 0; i < size; i++) {
            tanks[i].allowInput = machineConfig.getPortConfig("chestInput") != MachineConfig.PortConfig.NONE;
            tanks[i].allowOutput = machineConfig.getPortConfig("chestOutput") != MachineConfig.PortConfig.NONE;
        }
    }

    @Override
    public void onPreWork() {
        if (machineConfig.getPortConfig("chestOutput") == MachineConfig.PortConfig.ACTIVE) {
            machine.getNetwork()
                .map(network -> network.getComponent(AllNetworks.LOGISTICS_COMPONENT))
                .ifPresent(logistics -> logistics.addActiveFluid(PortDirection.OUTPUT, view));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.FLUID_STACK_HANDLER.get()) {
            return fluidHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("tanks", inner.serializeNBT());
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
        inner.deserializeNBT(tag.getCompound("tanks"));
        var tag1 = tag.getList("filters", Tag.TAG_COMPOUND);
        Arrays.fill(filters, null);
        for (var tag2 : tag1) {
            var tag3 = (CompoundTag) tag2;
            var slot = tag3.getInt("Slot");
            var stack = FluidStack.loadFluidStackFromNBT(tag3);
            filters[slot] = stack;
        }
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>> builder(Layout layout) {
        return CapabilityProviderBuilder.fromFactory("machine/tank", be -> new ElectricTank(be, layout));
    }
}