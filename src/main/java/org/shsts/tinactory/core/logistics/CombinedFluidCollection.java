package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedFluidCollection extends CombinedCollection implements IFluidCollection {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<IFluidCollection> composes = new ArrayList<>();
    public boolean allowInput = true;
    public boolean allowOutput = true;

    public CombinedFluidCollection(Collection<IFluidCollection> composes) {
        addComposes(composes);
    }

    public CombinedFluidCollection() {}

    private void addComposes(Collection<IFluidCollection> val) {
        composes.clear();
        composes.addAll(val);
        for (var compose : composes) {
            if (compose instanceof IPortNotifier notifier) {
                notifier.onUpdate(combinedListener);
            }
        }
    }

    public void setComposes(Collection<IFluidCollection> val) {
        for (var compose : composes) {
            if (compose instanceof IPortNotifier notifier) {
                notifier.unregisterListener(combinedListener);
            }
        }
        composes.clear();
        addComposes(val);
        invokeUpdate();
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        return allowInput && composes.stream().anyMatch($ -> $.acceptInput(stack));
    }

    @Override
    public boolean acceptOutput() {
        return allowOutput && composes.stream().anyMatch(IPort::acceptOutput);
    }

    @Override
    public int fill(FluidStack fluid, boolean simulate) {
        if (!allowInput) {
            return 0;
        }
        var stack = fluid.copy();
        for (var compose : composes) {
            if (stack.isEmpty()) {
                break;
            }
            var filled = compose.fill(stack, simulate);
            stack.shrink(filled);
        }
        return fluid.getAmount() - stack.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack fluid, boolean simulate) {
        if (!allowOutput) {
            return FluidStack.EMPTY;
        }
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
        return ret;
    }

    @Override
    public FluidStack drain(int limit, boolean simulate) {
        if (!allowOutput) {
            return FluidStack.EMPTY;
        }
        return composes.isEmpty() ? FluidStack.EMPTY :
            composes.get(0).drain(limit, simulate);
    }

    @Override
    public int getFluidAmount(FluidStack fluid) {
        return composes.stream().mapToInt($ -> $.getFluidAmount(fluid)).sum();
    }

    @Override
    public Collection<FluidStack> getAllFluids() {
        return composes.stream().flatMap($ -> $.getAllFluids().stream()).toList();
    }
}
