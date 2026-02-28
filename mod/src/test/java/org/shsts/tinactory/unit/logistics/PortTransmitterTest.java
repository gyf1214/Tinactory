package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.logistics.PortTransmitter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortTransmitterTest {
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
    void shouldProbeTransferWithoutMutation() {
        var from = new TestPort("iron", 10, 8);
        var to = new TestPort("iron", 5, 4);
        var transmitter = new PortTransmitter<>(STACK_ADAPTER);

        var moved = transmitter.probe(from, to, new TestStack("iron", 8), 6);

        assertEquals(1, moved.amount());
        assertEquals(8, from.stored);
        assertEquals(4, to.stored);
    }

    @Test
    void shouldSelectFirstTransferableCandidate() {
        var from = new TestPort("iron", 10, 5);
        var to = new TestPort("iron", 10, 0);
        var transmitter = new PortTransmitter<>(STACK_ADAPTER);

        var selected = transmitter.select(from, to,
            List.of(new TestStack("gold", 2), new TestStack("iron", 4)), 3);

        assertEquals("iron", selected.id());
        assertEquals(3, selected.amount());
    }

    @Test
    void shouldTransmitAndReturnRemainder() {
        var from = new TestPort("iron", 10, 5);
        var to = new TestPort("iron", 3, 2);
        var transmitter = new PortTransmitter<>(STACK_ADAPTER);

        var remainder = transmitter.transmit(from, to, new TestStack("iron", 4));

        assertEquals(1, from.stored);
        assertEquals(3, to.stored);
        assertEquals(3, remainder.amount());
    }

    private record TestStack(String id, int amount) {
        private TestStack copy() {
            return new TestStack(id, amount);
        }
    }

    private record TestKey(String id) implements IIngredientKey {}

    private static final class TestPort implements IPort<TestStack> {
        private final String id;
        private final int capacity;
        private int stored;

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
            return stack.amount() > 0 && Objects.equals(id, stack.id()) && stored < capacity;
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
            return List.of(new TestStack(id, stored));
        }
    }
}
