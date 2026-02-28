package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortFilter;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombinedPortTest {
    private static final IStackAdapter<TestStack> STACK_ADAPTER = new IStackAdapter<>() {
        @Override
        public TestStack empty() {
            return new TestStack("", 0);
        }

        @Override
        public boolean isEmpty(TestStack stack) {
            return stack.amount() <= 0;
        }

        @Override
        public TestStack copy(TestStack stack) {
            return stack.copy();
        }

        @Override
        public int amount(TestStack stack) {
            return stack.amount();
        }

        @Override
        public TestStack withAmount(TestStack stack, int amount) {
            return new TestStack(stack.id(), amount);
        }

        @Override
        public boolean canStack(TestStack left, TestStack right) {
            return Objects.equals(left.id(), right.id());
        }

        @Override
        public IIngredientKey keyOf(TestStack stack) {
            return new TestKey(stack.id());
        }
    };

    @Test
    void shouldFanOutInsertAcrossComposes() {
        var first = new TestPort("iron", 3, 0);
        var second = new TestPort("iron", 10, 0);
        var combined = new CombinedPort<>(STACK_ADAPTER, List.of(first, second));

        var remainder = combined.insert(new TestStack("iron", 8), false);

        assertEquals(0, remainder.amount());
        assertEquals(3, first.stored);
        assertEquals(5, second.stored);
    }

    @Test
    void shouldAggregateExtractAcrossComposes() {
        var first = new TestPort("iron", 10, 2);
        var second = new TestPort("iron", 10, 4);
        var combined = new CombinedPort<>(STACK_ADAPTER, List.of(first, second));

        var extracted = combined.extract(new TestStack("iron", 5), false);

        assertEquals("iron", extracted.id());
        assertEquals(5, extracted.amount());
        assertEquals(0, first.stored);
        assertEquals(1, second.stored);
    }

    @Test
    void shouldRespectInputAndOutputGates() {
        var port = new TestPort("iron", 10, 5);
        var combined = new CombinedPort<>(STACK_ADAPTER, List.of(port));
        combined.allowInput = false;
        combined.allowOutput = false;

        var remainder = combined.insert(new TestStack("iron", 2), false);
        var extracted = combined.extract(new TestStack("iron", 2), false);

        assertEquals(2, remainder.amount());
        assertEquals(0, extracted.amount());
        assertEquals(5, port.stored);
    }

    @Test
    void shouldKeepStateUnchangedWhenSimulating() {
        var first = new TestPort("iron", 10, 5);
        var second = new TestPort("iron", 10, 1);
        var combined = new CombinedPort<>(STACK_ADAPTER, List.of(first, second));

        var insertRemainder = combined.insert(new TestStack("iron", 7), true);
        var extracted = combined.extract(new TestStack("iron", 4), true);

        assertEquals(0, insertRemainder.amount());
        assertEquals(4, extracted.amount());
        assertEquals(5, first.stored);
        assertEquals(1, second.stored);
    }

    private record TestStack(String id, int amount) {
        private TestStack copy() {
            return new TestStack(id, amount);
        }
    }

    private record TestKey(String id) implements IIngredientKey {}

    private static final class TestPort implements IPort<TestStack>, IPortFilter<TestStack> {
        private final String id;
        private final int capacity;
        private int stored;
        private Predicate<TestStack> filter = $ -> true;

        private TestPort(String id, int capacity, int stored) {
            this.id = id;
            this.capacity = capacity;
            this.stored = stored;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return stack.amount() > 0 && Objects.equals(id, stack.id()) && stored < capacity && filter.test(stack);
        }

        @Override
        public boolean acceptOutput() {
            return stored > 0;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            if (!acceptInput(stack)) {
                return stack;
            }
            var inserted = Math.min(stack.amount(), capacity - stored);
            if (!simulate) {
                stored += inserted;
            }
            return new TestStack(stack.id(), stack.amount() - inserted);
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            if (!Objects.equals(id, stack.id()) || stack.amount() <= 0 || stored <= 0) {
                return new TestStack("", 0);
            }
            var moved = Math.min(stack.amount(), stored);
            if (!simulate) {
                stored -= moved;
            }
            return new TestStack(id, moved);
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            if (limit <= 0 || stored <= 0) {
                return new TestStack("", 0);
            }
            var moved = Math.min(limit, stored);
            if (!simulate) {
                stored -= moved;
            }
            return new TestStack(id, moved);
        }

        @Override
        public int getStorageAmount(TestStack stack) {
            return Objects.equals(id, stack.id()) ? stored : 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            if (stored <= 0) {
                return List.of();
            }
            var ret = new ArrayList<TestStack>();
            ret.add(new TestStack(id, stored));
            return ret;
        }

        @Override
        public void setFilters(List<? extends Predicate<TestStack>> filters) {
            filter = stack -> filters.stream().anyMatch($ -> $.test(stack));
        }

        @Override
        public void resetFilters() {
            filter = $ -> true;
        }
    }
}
