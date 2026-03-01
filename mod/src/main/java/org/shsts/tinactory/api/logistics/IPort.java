package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPort<T> {
    @SuppressWarnings("unchecked")
    static <T> IPort<T> empty() {
        return (IPort<T>) EMPTY;
    }

    PortType type();

    @SuppressWarnings("unchecked")
    default IPort<ItemStack> asItem() {
        assert type() == PortType.ITEM;
        return (IPort<ItemStack>) this;
    }

    @SuppressWarnings("unchecked")
    default IPort<FluidStack> asFluid() {
        assert type() == PortType.FLUID;
        return (IPort<FluidStack>) this;
    }

    @SuppressWarnings("unchecked")
    default IPortFilter<T> asFilter() {
        assert type() != PortType.NONE;
        if (this instanceof IPortFilter<?> filter) {
            return (IPortFilter<T>) filter;
        }
        throw new UnsupportedOperationException("Port does not support filters");
    }

    boolean acceptInput(T stack);

    T insert(T stack, boolean simulate);

    T extract(T stack, boolean simulate);

    T extract(int limit, boolean simulate);

    int getStorageAmount(T stack);

    Collection<T> getAllStorages();

    /**
     * If this returns false, extract and getItem should return empty.
     */
    boolean acceptOutput();

    IPort<?> EMPTY = new IPort<>() {
        @Override
        public PortType type() {
            return PortType.NONE;
        }

        @Override
        public boolean acceptInput(Object stack) {
            return false;
        }

        @Override
        public Object insert(Object stack, boolean simulate) {
            return stack;
        }

        @Override
        public Object extract(Object stack, boolean simulate) {
            return stack;
        }

        @Override
        public Object extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(Object stack) {
            return 0;
        }

        @Override
        public Collection<Object> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }
    };
}
