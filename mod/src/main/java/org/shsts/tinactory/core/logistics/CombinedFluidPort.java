package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidPort;

import java.util.Arrays;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidPort extends CombinedPort<FluidStack, IFluidPort> implements IFluidPort {
    private static final IPortAccess<FluidStack, IFluidPort> PORT_ACCESS = new IPortAccess<>() {
        @Override
        public boolean acceptInput(IFluidPort port, FluidStack stack) {
            return port.acceptInput(stack);
        }

        @Override
        public boolean acceptOutput(IFluidPort port) {
            return port.acceptOutput();
        }

        @Override
        public FluidStack insert(IFluidPort port, FluidStack stack, boolean simulate) {
            return port.insert(stack, simulate);
        }

        @Override
        public FluidStack extract(IFluidPort port, FluidStack stack, boolean simulate) {
            return port.extract(stack, simulate);
        }

        @Override
        public FluidStack extract(IFluidPort port, int limit, boolean simulate) {
            return port.extract(limit, simulate);
        }

        @Override
        public int getStorageAmount(IFluidPort port, FluidStack stack) {
            return port.getStorageAmount(stack);
        }

        @Override
        public Collection<FluidStack> getAllStorages(IFluidPort port) {
            return port.getAllStorages();
        }
    };

    private static final IStackAdapter<FluidStack> STACK_ADAPTER = new IStackAdapter<>() {
        @Override
        public FluidStack empty() {
            return FluidStack.EMPTY;
        }

        @Override
        public boolean isEmpty(FluidStack stack) {
            return stack.isEmpty();
        }

        @Override
        public FluidStack copy(FluidStack stack) {
            return stack.copy();
        }

        @Override
        public int amount(FluidStack stack) {
            return stack.getAmount();
        }

        @Override
        public FluidStack withAmount(FluidStack stack, int amount) {
            return StackHelper.copyWithAmount(stack, amount);
        }

        @Override
        public boolean canStack(FluidStack left, FluidStack right) {
            return left.isFluidEqual(right);
        }

        @Override
        public IIngredientKey keyOf(FluidStack stack) {
            return new FluidStackWrapper(stack);
        }
    };

    public CombinedFluidPort(IFluidPort... composes) {
        super(PORT_ACCESS, STACK_ADAPTER, Arrays.asList(composes));
    }

    public CombinedFluidPort() {
        super(PORT_ACCESS, STACK_ADAPTER);
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        return super.acceptInput(stack);
    }

    @Override
    public boolean acceptOutput() {
        return super.acceptOutput();
    }

    @Override
    public FluidStack insert(FluidStack stack, boolean simulate) {
        return super.insert(stack, simulate);
    }

    @Override
    public FluidStack extract(FluidStack fluid, boolean simulate) {
        return super.extract(fluid, simulate);
    }

    @Override
    public FluidStack extract(int limit, boolean simulate) {
        return super.extract(limit, simulate);
    }

    @Override
    public int getStorageAmount(FluidStack fluid) {
        return super.getStorageAmount(fluid);
    }

    @Override
    public Collection<FluidStack> getAllStorages() {
        return super.getAllStorages();
    }
}
