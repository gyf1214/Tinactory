package org.shsts.tinactory.unit.core.recipe;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackResultTest {
    @Test
    void shouldInsertScaledExactStackIntoMatchingPort() {
        var port = new TestStackPort(PortType.ITEM, TestStack.item("ingot", 1), 16);
        var result = new StackResult<>("test_stack_result", PortType.ITEM, 1d,
            TestStack.item("ingot", 3), TestStack.ADAPTER);

        var inserted = result.insertPort(port, 2, new Random(1L), false);

        assertTrue(inserted.isPresent());
        assertEquals(new TestStack(PortType.ITEM, "ingot", "", 6),
            assertInstanceOf(StackResult.class, inserted.orElseThrow()).stack());
        assertEquals(TestStack.item("ingot", 7), port.stored());
    }

    @Test
    void shouldReturnEmptyWhenChanceProducesNoOutput() {
        var port = new TestStackPort(PortType.ITEM, TestStack.item("ingot", 1), 16);
        var result = new StackResult<>("test_stack_result", PortType.ITEM, 0d,
            TestStack.item("ingot", 3), TestStack.ADAPTER);

        var inserted = result.insertPort(port, 4, new Random(1L), false);

        assertTrue(inserted.isEmpty());
        assertEquals(TestStack.item("ingot", 1), port.stored());
    }

    @Test
    void shouldExposeAdapterDrivenFilter() {
        var result = new StackResult<>("test_stack_result", PortType.FLUID, 1d,
            new TestStack(PortType.FLUID, "steam", "hot", 1000), TestStack.ADAPTER);

        @SuppressWarnings("unchecked")
        var filter = (java.util.function.Predicate<TestStack>) result.filter();

        assertTrue(filter.test(new TestStack(PortType.FLUID, "steam", "hot", 250)));
        assertFalse(filter.test(new TestStack(PortType.FLUID, "steam", "", 250)));
        assertFalse(filter.test(TestStack.item("steam", 1)));
    }

    private static final class TestStackPort implements IPort<TestStack> {
        private final PortType type;
        private final int capacity;
        private TestStack stored;

        private TestStackPort(PortType type, TestStack stored, int capacity) {
            this.type = type;
            this.stored = stored;
            this.capacity = capacity;
        }

        @Override
        public PortType type() {
            return type;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return type == stack.type() &&
                TestStack.ADAPTER.canStack(stored, stack) &&
                stored.amount() < capacity;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            if (!acceptInput(stack)) {
                return stack;
            }
            var inserted = Math.min(stack.amount(), capacity - stored.amount());
            if (!simulate) {
                stored = TestStack.ADAPTER.withAmount(stored, stored.amount() + inserted);
            }
            return TestStack.ADAPTER.withAmount(stack, stack.amount() - inserted);
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
            return type == stack.type() && TestStack.ADAPTER.canStack(stored, stack) ? stored.amount() : 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return stored.amount() > 0 ? List.of(stored) : List.of();
        }

        @Override
        public boolean acceptOutput() {
            return stored.amount() > 0;
        }

        private TestStack stored() {
            return stored;
        }
    }
}
