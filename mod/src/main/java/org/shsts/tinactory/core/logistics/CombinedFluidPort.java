package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.Arrays;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidPort extends CombinedPort<FluidStack> implements IPort<FluidStack> {
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

    public CombinedFluidPort(IPort<FluidStack>... composes) {
        super(STACK_ADAPTER, Arrays.asList(composes));
    }

    public CombinedFluidPort() {
        super(STACK_ADAPTER);
    }

    @Override
    public PortType type() {
        return PortType.FLUID;
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
