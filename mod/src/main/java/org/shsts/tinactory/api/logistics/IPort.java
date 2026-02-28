package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPort<T> extends IPortFilter<T> {
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
    default <TFilter> IPortFilter<TFilter> asFilter() {
        assert type() != PortType.NONE;
        return (IPortFilter<TFilter>) this;
    }

    @SuppressWarnings("unchecked")
    default IPort<FluidStack> asFluid() {
        assert type() == PortType.FLUID;
        return (IPort<FluidStack>) this;
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

    @Override
    default void setFilters(List<? extends Predicate<T>> filters) {}

    @Override
    default void resetFilters() {}

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
