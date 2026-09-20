package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.integration.logistics.IFluidTanksHandler;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.function.Predicate;

import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTank extends ElectricStorage<FluidStack> implements INBTSerializable<CompoundTag> {
    public static final String ID = "machine/tank";

    private final class VirtualTank implements IFluidTank {
        private final int index;

        private VirtualTank(int index) {
            this.index = index;
        }

        @Override
        public FluidStack getFluid() {
            return getStackInVirtualSlot(index);
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
            var remaining = insertIntoVirtualSlot(index, resource, action.simulate());
            return resource.getAmount() - remaining.getAmount();
        }

        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            return extractFromVirtualSlot(index, resource, action.simulate(), $ -> Integer.MAX_VALUE);
        }

        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return extractFromVirtualSlot(index, maxDrain, action.simulate(), $ -> Integer.MAX_VALUE);
        }
    }

    private final IFluidTanksHandler fluidHandler = new IFluidTanksHandler() {
        @Override
        public int getTanks() {
            return virtualSlots();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return getStackInVirtualSlot(tank);
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
            var remaining = port().insert(resource, action.simulate());
            return resource.getAmount() - remaining.getAmount();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return port().extract(resource, action.simulate());
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return port().extract(maxDrain, action.simulate());
        }
    };

    public ElectricTank(BlockEntity blockEntity, Properties properties) {
        super(blockEntity, PortType.FLUID, StackHelper.FLUID_ADAPTER, properties);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.container(ID, be -> new ElectricTank(be, properties));
    }

    @Override
    protected Predicate<FluidStack> asPredicate(HolderLookup.Provider provider, FilterEntry entry) {
        return entry::testFluid;
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        super.attachCapability(builder);
        builder.attach(FLUID_HANDLER, fluidHandler);
    }

    @Override
    protected CompoundTag serializeStack(HolderLookup.Provider provider, FluidStack stack) {
        return (CompoundTag) stack.save(provider);
    }

    @Override
    protected FluidStack deserializeStack(HolderLookup.Provider provider, CompoundTag tag) {
        return FluidStack.parseOptional(provider, tag);
    }
}
