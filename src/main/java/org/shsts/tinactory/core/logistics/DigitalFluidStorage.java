package org.shsts.tinactory.core.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IFluidFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_COLLECTION;
import static org.shsts.tinactory.core.logistics.StackHelper.TRUE_FLUID_FILTER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalFluidStorage extends DigitalStorage
    implements IFluidCollection, IFluidFilter, INBTSerializable<CompoundTag> {
    private record FluidStackWrapper(FluidStack stack) {
        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof FluidStackWrapper wrapper &&
                stack.isFluidEqual(wrapper.stack));
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getFluid(), stack.getTag());
        }
    }

    private final Map<FluidStackWrapper, FluidStack> fluids = new HashMap<>();
    private Predicate<FluidStack> filter = TRUE_FLUID_FILTER;

    public DigitalFluidStorage(int bytesLimit) {
        super(bytesLimit);
    }

    @Override
    public boolean acceptInput(FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (!filter.test(stack) || bytesRemaining < CONFIG.bytesPerFluid.get()) {
            return false;
        }
        if (!fluids.containsKey(new FluidStackWrapper(stack))) {
            return bytesRemaining >= CONFIG.bytesPerFluid.get() + CONFIG.bytesPerFluidType.get();
        }
        return true;
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public int fill(FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty() || !acceptInput(fluid)) {
            return 0;
        }
        var key = new FluidStackWrapper(fluid);
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        if (!fluids.containsKey(key)) {
            var newBytesRemaining = bytesRemaining - CONFIG.bytesPerFluidType.get();
            var inserted = Math.min(fluid.getAmount(), newBytesRemaining / bytesPerFluid);
            assert inserted > 0 && inserted <= fluid.getAmount();
            if (!simulate) {
                var insertedStack = StackHelper.copyWithAmount(fluid, inserted);
                fluids.put(new FluidStackWrapper(insertedStack), insertedStack);
                bytesRemaining = newBytesRemaining - inserted * bytesPerFluid;
            }
            return inserted;
        } else {
            var inserted = Math.min(fluid.getAmount(), bytesRemaining / bytesPerFluid);
            assert inserted > 0 && inserted <= fluid.getAmount();
            if (!simulate) {
                fluids.get(key).grow(inserted);
                bytesRemaining -= inserted * bytesPerFluid;
            }
            return inserted;
        }
    }

    @Override
    public FluidStack drain(FluidStack fluid, boolean simulate) {
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
                bytesRemaining += CONFIG.bytesPerFluidType.get() + bytesPerFluid * stack.getAmount();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(fluid.getAmount());
                bytesRemaining += bytesPerFluid * fluid.getAmount();
            }
            return fluid.copy();
        }
    }

    @Override
    public FluidStack drain(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput() || fluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var stack = fluids.values().iterator().next();
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        if (limit >= stack.getAmount()) {
            if (!simulate) {
                fluids.remove(new FluidStackWrapper(stack));
                bytesRemaining += CONFIG.bytesPerFluidType.get() + bytesPerFluid * stack.getAmount();
            }
            return stack.copy();
        } else {
            if (!simulate) {
                stack.shrink(limit);
                bytesRemaining += bytesPerFluid * limit;
            }
            return StackHelper.copyWithAmount(stack, limit);
        }
    }

    @Override
    public int getFluidAmount(FluidStack fluid) {
        if (!acceptOutput()) {
            return 0;
        }
        var stack = fluids.get(new FluidStackWrapper(fluid));
        return stack == null ? 0 : stack.getAmount();
    }

    @Override
    public Collection<FluidStack> getAllFluids() {
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
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == FLUID_COLLECTION.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var listTag = new ListTag();
        for (var stack : fluids.values()) {
            var fluidTag = new CompoundTag();
            stack.writeToNBT(fluidTag);
            listTag.add(fluidTag);
        }
        tag.put("Fluids", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        fluids.clear();
        bytesRemaining = bytesLimit;

        var listTag = tag.getList("Fluids", Tag.TAG_COMPOUND);
        var bytesPerFluid = CONFIG.bytesPerFluid.get();
        var bytesPerType = CONFIG.bytesPerFluidType.get();
        for (var fluidTag : listTag) {
            var stack = FluidStack.loadFluidStackFromNBT((CompoundTag) fluidTag);
            fluids.put(new FluidStackWrapper(stack), stack);
            bytesRemaining -= bytesPerType + bytesPerFluid * stack.getAmount();
        }
    }
}
