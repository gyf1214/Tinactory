package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidCollection extends CombinedCollection implements IFluidCollection {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<IFluidCollection> composes = new ArrayList<>();

    public void setComposes(Collection<IFluidCollection> val) {
        composes.clear();
        composes.addAll(val);
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        return composes.stream().anyMatch($ -> $.acceptInput(stack));
    }

    @Override
    public boolean acceptOutput() {
        return composes.stream().anyMatch(IPort::acceptOutput);
    }

    @Override
    public int fill(FluidStack fluid, boolean simulate) {
        var stack = fluid.copy();
        for (var compose : composes) {
            if (stack.isEmpty()) {
                break;
            }
            var filled = compose.fill(stack, simulate);
            stack.shrink(filled);
        }
        if (!simulate && stack.getAmount() < fluid.getAmount()) {
            invokeUpdate();
        }
        return fluid.getAmount() - stack.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack fluid, boolean simulate) {
        var stack = fluid.copy();
        var ret = FluidStack.EMPTY;
        for (var compose : composes) {
            if (stack.isEmpty()) {
                break;
            }
            var stack1 = compose.drain(stack, simulate);
            if (!stack1.isEmpty()) {
                if (ret.isEmpty()) {
                    ret = stack1;
                } else if (ret.isFluidEqual(stack1)) {
                    ret.grow(stack1.getAmount());
                } else {
                    // don't know what to do actually, can only destroy the extracted fluid
                    LOGGER.warn("{}: Extracted fluid {} cannot stack with required fluid {}",
                        this, stack1, ret);
                    continue;
                }
                stack.shrink(stack1.getAmount());
            }
        }
        if (!simulate && !ret.isEmpty()) {
            invokeUpdate();
        }
        return ret;
    }

    @Override
    public FluidStack drain(int limit, boolean simulate) {
        var ret = composes.isEmpty() ? FluidStack.EMPTY :
            composes.get(0).drain(limit, simulate);
        if (!simulate && !ret.isEmpty()) {
            invokeUpdate();
        }
        return ret;
    }

    @Override
    public int getFluidAmount(FluidStack fluid) {
        return composes.stream().mapToInt($ -> $.getFluidAmount(fluid)).sum();
    }

    @Override
    public Collection<FluidStack> getAllFluids() {
        return composes.stream().flatMap($ -> $.getAllFluids().stream()).toList();
    }

    @Override
    public void setFluidFilter(List<? extends Predicate<FluidStack>> filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetFluidFilter() {
        throw new UnsupportedOperationException();
    }
}
