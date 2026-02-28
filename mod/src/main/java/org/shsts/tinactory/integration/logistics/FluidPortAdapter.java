package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.logistics.StackHelper;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FluidPortAdapter implements IStackAdapter<FluidStack> {
    public static final FluidPortAdapter INSTANCE = new FluidPortAdapter();

    private FluidPortAdapter() {}

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
        return new FluidKey(stack);
    }

    private static final class FluidKey implements IIngredientKey {
        private final FluidStack stack;

        private FluidKey(FluidStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof FluidKey key && stack.isFluidEqual(key.stack));
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getFluid(), stack.getTag());
        }
    }
}
