package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.logistics.IFluidTanksHandler;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MENU_FLUID_HANDLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTank extends ElectricStorage<FluidStack> implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ID = "machine/tank";

    private final class VirtualTank implements IFluidTank {
        private final int index;

        private VirtualTank(int index) {
            this.index = index;
        }

        @Override
        public FluidStack getFluid() {
            return stack(virtualEntries().get(index));
        }

        @Override
        public int getFluidAmount() {
            return getFluid().getAmount();
        }

        @Override
        public int getCapacity() {
            return stackLimit();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return validForVirtualSlot(index, stack);
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return insertIntoVirtualSlot(index, resource, action.simulate());
        }

        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            var current = getFluid();
            if (!FluidStack.isSameFluidSameComponents(current, resource)) {
                return FluidStack.EMPTY;
            }
            return extractFromVirtualSlot(index, resource.getAmount(), action.simulate());
        }

        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return extractFromVirtualSlot(index, maxDrain, action.simulate());
        }
    }

    private final IFluidTanksHandler fluidHandler = new IFluidTanksHandler() {
        @Override
        public int getTanks() {
            return storageSlots();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return stack(virtualEntries().get(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return stackLimit();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return validForVirtualSlot(tank, stack);
        }

        @Override
        public IFluidTank getTank(int index) {
            return new VirtualTank(index);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            var remaining = insert(resource, action.simulate());
            return resource.getAmount() - remaining.getAmount();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return extract(resource, action.simulate());
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return extract(maxDrain, action.simulate());
        }
    };

    public ElectricTank(BlockEntity blockEntity, int storageSlots, int stackLimit, double power) {
        super(blockEntity, StackHelper.FLUID_ADAPTER, storageSlots, stackLimit, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        int storageSlots, int stackLimit, double power) {
        return $ -> $.container(ID, be -> new ElectricTank(be, storageSlots, stackLimit, power));
    }

    @Override
    public PortType type() {
        return PortType.FLUID;
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        super.attachCapability(builder);
        builder.attach(FLUID_HANDLER, fluidHandler);
        builder.attach(MENU_FLUID_HANDLER, fluidHandler);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return serializeEntries(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (deserializeEntries(provider, tag)) {
            return;
        }
        var tanks = tag.getCompound("tanks").getList("Tanks", Tag.TAG_COMPOUND);
        for (var value : tanks) {
            var fluid = FluidStack.parseOptional(provider, (CompoundTag) value);
            if (!fluid.isEmpty() && !loadEntry(new StorageEntry(StackHelper.FLUID_ADAPTER.keyOf(fluid),
                fluid.getAmount(), false))) {
                LOGGER.warn("Discarding overflowing legacy Electric Tank fluid {}", fluid);
            }
        }
        var filters = tag.getList("filters", Tag.TAG_COMPOUND);
        for (var value : filters) {
            var fluid = FluidStack.parseOptional(provider, (CompoundTag) value);
            if (!fluid.isEmpty() && !loadEntry(new StorageEntry(StackHelper.FLUID_ADAPTER.keyOf(fluid), 0, true))) {
                LOGGER.warn("Discarding overflowing legacy Electric Tank filter {}", fluid);
            }
        }
    }
}
