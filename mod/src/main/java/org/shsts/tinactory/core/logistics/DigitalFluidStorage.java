package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidPort;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.core.logistics.StackHelper.TRUE_FLUID_FILTER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalFluidStorage extends PortNotifier implements IFluidPort, INBTSerializable<CompoundTag> {
    private final IDigitalProvider provider;
    public int maxAmount = Integer.MAX_VALUE;
    private final Map<FluidStackWrapper, FluidStack> fluids = new HashMap<>();
    private Predicate<FluidStack> filter = TRUE_FLUID_FILTER;

    public DigitalFluidStorage(IDigitalProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (!filter.test(stack)) {
            return false;
        }
        var key = new FluidStackWrapper(stack);
        if (fluids.containsKey(key)) {
            return fluids.get(key).getAmount() < maxAmount && provider.canConsume(CONFIG.bytesPerFluid.get());
        } else {
            return provider.canConsume(CONFIG.bytesPerFluid.get() + CONFIG.bytesPerFluidType.get());
        }
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public FluidStack insert(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty() || !acceptInput(fluid)) {
            return fluid;
        }
        var key = new FluidStackWrapper(fluid);
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        if (!fluids.containsKey(key)) {
            var bytesPerType = CONFIG.bytesPerFluidType.get();
            var limit = Math.min(provider.consumeLimit(bytesPerType, bytesPerFluid), maxAmount);
            var inserted = Math.min(fluid.getAmount(), limit);
            assert inserted > 0 && inserted <= fluid.getAmount();
            var remaining = StackHelper.copyWithAmount(fluid, fluid.getAmount() - inserted);
            if (!simulate) {
                var insertedStack = StackHelper.copyWithAmount(fluid, inserted);
                fluids.put(new FluidStackWrapper(insertedStack), insertedStack);
                provider.consume(bytesPerType + inserted * bytesPerFluid);
                invokeUpdate();
            }
            return remaining;
        } else {
            var existing = fluids.get(key);
            var limit = Math.min(provider.consumeLimit(bytesPerFluid), maxAmount - existing.getAmount());
            var inserted = Math.min(fluid.getAmount(), limit);
            assert inserted > 0 && inserted <= fluid.getAmount();
            var remaining = StackHelper.copyWithAmount(fluid, fluid.getAmount() - inserted);
            if (!simulate) {
                existing.grow(inserted);
                provider.consume(inserted * bytesPerFluid);
                invokeUpdate();
            }
            return remaining;
        }
    }

    @Override
    public FluidStack extract(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty() || !acceptOutput()) {
            return FluidStack.EMPTY;
        }
        var key = new FluidStackWrapper(fluid);
        if (!fluids.containsKey(key)) {
            return FluidStack.EMPTY;
        }
        var stack = fluids.get(key);
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        if (fluid.getAmount() >= stack.getAmount()) {
            if (!simulate) {
                fluids.remove(key);
                provider.restore(CONFIG.bytesPerFluidType.get() + bytesPerFluid * stack.getAmount());
                invokeUpdate();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(fluid.getAmount());
                provider.restore(bytesPerFluid * fluid.getAmount());
                invokeUpdate();
            }
            return fluid.copy();
        }
    }

    @Override
    public FluidStack extract(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput() || fluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var stack = fluids.values().iterator().next();
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        if (limit >= stack.getAmount()) {
            if (!simulate) {
                fluids.remove(new FluidStackWrapper(stack));
                provider.restore(CONFIG.bytesPerFluidType.get() + bytesPerFluid * stack.getAmount());
                invokeUpdate();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(limit);
                provider.restore(bytesPerFluid * limit);
                invokeUpdate();
            }
            return StackHelper.copyWithAmount(stack, limit);
        }
    }

    @Override
    public int getStorageAmount(FluidStack fluid) {
        if (!acceptOutput()) {
            return 0;
        }
        var stack = fluids.get(new FluidStackWrapper(fluid));
        return stack == null ? 0 : stack.getAmount();
    }

    @Override
    public Collection<FluidStack> getAllStorages() {
        return acceptOutput() ? fluids.values() : Collections.emptyList();
    }

    @Override
    public void setFilters(List<? extends Predicate<FluidStack>> filters) {
        filter = stack -> filters.stream().anyMatch($ -> $.test(stack));
    }

    @Override
    public void resetFilters() {
        filter = TRUE_FLUID_FILTER;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var listTag = new ListTag();
        for (var stack : fluids.values()) {
            listTag.add(StackHelper.serializeFluidStack(stack));
        }
        tag.put("Fluids", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        provider.reset();
        var listTag = tag.getList("Fluids", Tag.TAG_COMPOUND);
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        var bytesPerType = CONFIG.bytesPerFluidType.get();
        for (var fluidTag : listTag) {
            var stack = FluidStack.loadFluidStackFromNBT((CompoundTag) fluidTag);
            fluids.put(new FluidStackWrapper(stack), stack);
            provider.consume(bytesPerType + bytesPerFluid * stack.getAmount());
        }
    }
}
