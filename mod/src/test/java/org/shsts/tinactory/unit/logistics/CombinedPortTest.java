package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinedPortTest {
    private static class TestCombinedPort extends CombinedPort<TestStack> {
        public TestCombinedPort(Collection<IPort<TestStack>> composes) {
            this(composes, true);
        }

        public TestCombinedPort(Collection<IPort<TestStack>> composes, boolean delegateChildUpdates) {
            super(TestStack.ADAPTER, composes, delegateChildUpdates);
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }
    }

    @Test
    void shouldFanOutInsertAcrossComposes() {
        var first = new TestPort("iron", 3, 0);
        var second = new TestPort("iron", 10, 0);
        var combined = new TestCombinedPort(List.of(first, second));

        var remainder = combined.insert(new TestStack("iron", 8), false);

        assertEquals(0, remainder.amount());
        assertEquals(3, first.stored());
        assertEquals(5, second.stored());
    }

    @Test
    void shouldAggregateExtractAcrossComposes() {
        var first = new TestPort("iron", 10, 2);
        var second = new TestPort("iron", 10, 4);
        var combined = new TestCombinedPort(List.of(first, second));

        var extracted = combined.extract(new TestStack("iron", 5), false);

        assertEquals("iron", extracted.id());
        assertEquals(5, extracted.amount());
        assertEquals(0, first.stored());
        assertEquals(1, second.stored());
    }

    @Test
    void shouldRespectInputAndOutputGates() {
        var port = new TestPort("iron", 10, 5);
        var combined = new TestCombinedPort(List.of(port));
        combined.allowInput = false;
        combined.allowOutput = false;

        var remainder = combined.insert(new TestStack("iron", 2), false);
        var extracted = combined.extract(new TestStack("iron", 2), false);

        assertEquals(2, remainder.amount());
        assertEquals(0, extracted.amount());
        assertEquals(5, port.stored());
    }

    @Test
    void shouldKeepStateUnchangedWhenSimulating() {
        var first = new TestPort("iron", 10, 5);
        var second = new TestPort("iron", 10, 1);
        var combined = new TestCombinedPort(List.of(first, second));

        var insertRemainder = combined.insert(new TestStack("iron", 7), true);
        var extracted = combined.extract(new TestStack("iron", 4), true);

        assertEquals(0, insertRemainder.amount());
        assertEquals(4, extracted.amount());
        assertEquals(5, first.stored());
        assertEquals(1, second.stored());
    }

    @Test
    void extractWithLimitShouldReturnEmptyForInvalidOrEmptyComposes() {
        var withChild = new TestCombinedPort(List.of(new TestPort("iron", 10, 5)));

        assertEquals(0, withChild.extract(0, false).amount());
        assertEquals(0, new TestCombinedPort(List.of()).extract(3, false).amount());
    }

    @Test
    void extractWithLimitShouldOnlyUseFirstCompose() {
        var first = new TestPort("iron", 10, 0);
        var second = new TestPort("iron", 10, 5);
        var combined = new TestCombinedPort(List.of(first, second));

        var extracted = combined.extract(3, false);

        assertEquals(0, extracted.amount());
        assertEquals(0, first.stored());
        assertEquals(5, second.stored());
    }

    @Test
    void shouldRebindListenersWhenComposesChange() {
        var first = new TestPort("iron", 10, 2);
        var second = new TestPort("iron", 10, 2);
        var combined = new TestCombinedPort(List.of(first));
        var updates = new AtomicInteger();
        combined.onUpdate(updates::incrementAndGet);

        first.extract(new TestStack("iron", 1), false);
        combined.setComposes(List.of(second));
        first.extract(new TestStack("iron", 1), false);
        second.extract(new TestStack("iron", 1), false);

        assertEquals(3, updates.get());
    }

    @Test
    void operationOwnedModeShouldNotifyOnceForSuccessfulOperations() {
        var port = new TestPort("iron", 10, 3);
        var combined = new TestCombinedPort(List.of(port), false);
        var updates = new AtomicInteger();
        combined.onUpdate(updates::incrementAndGet);

        combined.insert(new TestStack("iron", 2), false);
        combined.extract(new TestStack("iron", 1), false);
        combined.extract(1, false);

        assertEquals(3, updates.get());
    }

    @Test
    void operationOwnedModeShouldNotNotifyForSimulationOrNoOp() {
        var port = new TestPort("iron", 10, 3);
        var combined = new TestCombinedPort(List.of(port), false);
        var updates = new AtomicInteger();
        combined.onUpdate(updates::incrementAndGet);

        combined.insert(new TestStack("gold", 1), false);
        combined.insert(new TestStack("iron", 1), true);
        combined.extract(new TestStack("gold", 1), false);
        combined.extract(new TestStack("iron", 1), true);
        combined.extract(0, false);
        combined.extract(1, false);

        assertEquals(1, updates.get());
    }

    @Test
    void operationOwnedModeShouldNotForwardDirectChildNotifications() {
        var port = new TestPort("iron", 10, 1);
        var combined = new TestCombinedPort(List.of(port), false);
        var updates = new AtomicInteger();
        combined.onUpdate(updates::incrementAndGet);

        port.extract(new TestStack("iron", 1), false);

        assertEquals(0, updates.get());
    }

    @Test
    void shouldContinueAfterIncompatibleChildExtract() {
        var first = new TestPort("iron", 10, 1);
        var incompatible = new IncompatibleExtractPort();
        var second = new TestPort("iron", 10, 1);
        var combined = new TestCombinedPort(List.of(first, incompatible, second));

        var extracted = combined.extract(new TestStack("iron", 2), false);

        assertEquals("iron", extracted.id());
        assertEquals(2, extracted.amount());
        assertTrue(incompatible.called);
        assertEquals(0, first.stored());
        assertEquals(0, second.stored());
    }

    @Test
    void shouldForwardAndResetFiltersAcrossFilterCapableChildren() {
        var first = new TestPort("iron", 10, 0);
        var second = new TestPort("gold", 10, 0);
        var combined = new TestCombinedPort(List.of(first, second));

        combined.setFilters(List.of(stack -> stack.id().equals("gold")));

        assertEquals(1, combined.insert(new TestStack("iron", 1), false).amount());
        assertEquals(0, combined.insert(new TestStack("gold", 1), false).amount());

        combined.resetFilters();

        assertEquals(0, combined.insert(new TestStack("iron", 1), false).amount());
        assertEquals(1, first.stored());
        assertEquals(1, second.stored());
    }

    @Test
    void getStorageAmountShouldAggregateBeyondIntegerMaxValue() {
        var combined = new TestCombinedPort(List.of(
            new AmountPort(Integer.MAX_VALUE),
            new AmountPort(1)));

        var amount = combined.getStorageAmount(new TestStack("iron", 1));

        assertEquals((long) Integer.MAX_VALUE + 1L, amount);
    }

    private static final class IncompatibleExtractPort implements IPort<TestStack> {
        private boolean called;

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
            return stack;
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            called = true;
            return new TestStack("gold", Math.min(1, stack.amount()));
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            return TestStack.ADAPTER.empty();
        }

        @Override
        public long getStorageAmount(TestStack stack) {
            return 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return true;
        }
    }

    private record AmountPort(int amount) implements IPort<TestStack> {
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
            return stack;
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            return TestStack.ADAPTER.empty();
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            return TestStack.ADAPTER.empty();
        }

        @Override
        public long getStorageAmount(TestStack stack) {
            return amount;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return true;
        }
    }
}
