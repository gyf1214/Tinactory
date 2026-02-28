package org.shsts.tinactory.unit;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortFilter;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortFilterApiTest {
    @Test
    void itemPortShouldSupportTypedFilterAccessFromPort() {
        var port = new TestItemPort();
        IPort base = port;
        IPortFilter<ItemStack> filter = base.asFilter();

        filter.setFilters(List.<Predicate<ItemStack>>of($ -> true));
        filter.resetFilters();

        assertTrue(port.setInvoked);
        assertTrue(port.resetInvoked);
        assertSame(port, base.asItem());
    }

    @Test
    void fluidPortShouldExposeFilterAsTypedInterface() {
        var port = new TestFluidPort();
        IPortFilter<FluidStack> filter = port.asFilter();

        filter.setFilters(List.<Predicate<FluidStack>>of($ -> true));
        filter.resetFilters();

        assertTrue(port.setInvoked);
        assertTrue(port.resetInvoked);
        assertSame(port, port.asFluid());
    }

    private static final class TestItemPort implements IItemPort {
        private boolean setInvoked;
        private boolean resetInvoked;

        @Override
        public boolean acceptInput(ItemStack stack) {
            return true;
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extract(ItemStack item, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(ItemStack item) {
            return 0;
        }

        @Override
        public Collection<ItemStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }

        @Override
        public void setFilters(List<? extends Predicate<ItemStack>> filters) {
            setInvoked = true;
        }

        @Override
        public void resetFilters() {
            resetInvoked = true;
        }
    }

    private static final class TestFluidPort implements IFluidPort {
        private boolean setInvoked;
        private boolean resetInvoked;

        @Override
        public boolean acceptInput(FluidStack stack) {
            return true;
        }

        @Override
        public FluidStack insert(FluidStack fluid, boolean simulate) {
            return fluid;
        }

        @Override
        public FluidStack extract(FluidStack fluid, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FluidStack extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(FluidStack fluid) {
            return 0;
        }

        @Override
        public Collection<FluidStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }

        @Override
        public void setFilters(List<? extends Predicate<FluidStack>> filters) {
            setInvoked = true;
        }

        @Override
        public void resetFilters() {
            resetInvoked = true;
        }
    }
}
