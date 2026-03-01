package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortFilter;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortFilterApiTest {
    @Test
    void portShouldSupportTypedFilterAccessFromPort() {
        var port = new TestFilterPort();
        IPort<TestStack> base = port;
        IPortFilter<TestStack> filter = base.asFilter();

        filter.setFilters(List.of($ -> true));
        filter.resetFilters();

        assertTrue(port.setInvoked);
        assertTrue(port.resetInvoked);
        assertSame(port, base);
    }

    @Test
    void portWithoutFilterShouldThrowOnAsFilter() {
        assertThrows(UnsupportedOperationException.class, () -> new NonFilterPort().asFilter());
    }

    private static final class TestFilterPort implements IPort<TestStack>, IPortFilter<TestStack> {
        private boolean setInvoked;
        private boolean resetInvoked;

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return false;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(TestStack stack) {
            return 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }

        @Override
        public void setFilters(List<? extends Predicate<TestStack>> filters) {
            setInvoked = true;
        }

        @Override
        public void resetFilters() {
            resetInvoked = true;
        }
    }

    private static final class NonFilterPort implements IPort<TestStack> {
        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return false;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(TestStack stack) {
            return 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }
    }
}
