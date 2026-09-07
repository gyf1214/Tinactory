package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.PortTransmitter;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortTransmitterTest {
    @Test
    void shouldTransmitAcrossCandidatesUntilAggregateLimit() {
        var from = new MultiStackPort(20, TestStack.item("iron", 5), TestStack.item("gold", 7));
        var to = new MultiStackPort(20);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var moved = transmitter.transmit(from, to, $ -> true, 10);

        assertEquals(10, moved);
        assertEquals(0, from.amount("iron"));
        assertEquals(2, from.amount("gold"));
        assertEquals(5, to.amount("iron"));
        assertEquals(5, to.amount("gold"));
    }

    @Test
    void shouldSkipExcludedAndUntransferableCandidates() {
        var from = new MultiStackPort(20, TestStack.item("blocked", 4), TestStack.item("iron", 4),
            TestStack.item("gold", 4));
        var to = new MultiStackPort(20, Set.of("iron", "gold"));
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var moved = transmitter.transmit(from, to, stack -> !stack.id().equals("gold"), 8);

        assertEquals(4, moved);
        assertEquals(4, from.amount("blocked"));
        assertEquals(0, from.amount("iron"));
        assertEquals(4, from.amount("gold"));
        assertEquals(4, to.amount("iron"));
        assertEquals(0, to.amount("gold"));
    }

    @Test
    void shouldRespectSourceExhaustionDestinationCapacityAndPartialMovement() {
        var from = new MultiStackPort(20, TestStack.item("iron", 5), TestStack.item("gold", 7));
        var to = new MultiStackPort(8);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var moved = transmitter.transmit(from, to, $ -> true, 20);

        assertEquals(8, moved);
        assertEquals(0, from.amount("iron"));
        assertEquals(4, from.amount("gold"));
        assertEquals(5, to.amount("iron"));
        assertEquals(3, to.amount("gold"));
    }

    @Test
    void shouldReturnZeroWhenNothingCanTransferOrLimitIsNonPositive() {
        var from = new MultiStackPort(20, TestStack.item("iron", 5));
        var full = new MultiStackPort(0);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        assertEquals(0, transmitter.transmit(from, full, $ -> true, 5));
        assertEquals(0, transmitter.transmit(from, new MultiStackPort(20), $ -> true, 0));
        assertEquals(0, transmitter.transmit(from, new MultiStackPort(20), $ -> true, -1));
        assertEquals(5, from.amount("iron"));
    }

    @Test
    void shouldSnapshotCandidatesOnceAndPreserveCandidateOrder() {
        var from = new MultiStackPort(20, TestStack.item("first", 4), TestStack.item("second", 4));
        var to = new MultiStackPort(20);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var moved = transmitter.transmit(from, to, $ -> true, 6);

        assertEquals(6, moved);
        assertEquals(1, from.snapshotCalls());
        assertEquals(List.of("first", "second"), to.ids());
        assertEquals(4, to.amount("first"));
        assertEquals(2, to.amount("second"));
    }

    @Test
    void shouldTransmitOnlyRequestedIdentityUsingRequestedLimit() {
        var from = new MultiStackPort(20, TestStack.fluid("water", 10), TestStack.fluid("lava", 3));
        var to = new MultiStackPort(20);
        var transmitter = new PortTransmitter<>(TestStack.ADAPTER);

        var moved = transmitter.transmitIdentity(from, to, TestStack.fluid("water", 1), 6);

        assertEquals(6, moved);
        assertEquals(4, from.amount("water"));
        assertEquals(3, from.amount("lava"));
        assertEquals(6, to.amount("water"));
        assertEquals(0, to.amount("lava"));
        assertEquals(0, from.snapshotCalls());
    }

    private record StackId(PortType type, String id, String nbt) {}

    private static final class MultiStackPort implements IPort<TestStack> {
        private final int capacity;
        private final Set<String> acceptedIds;
        private final Map<StackId, Integer> entries = new LinkedHashMap<>();
        private int snapshotCalls;

        private MultiStackPort(int capacity, TestStack... stacks) {
            this(capacity, Set.of(), stacks);
        }

        private MultiStackPort(int capacity, Set<String> acceptedIds, TestStack... stacks) {
            this.capacity = capacity;
            this.acceptedIds = acceptedIds;
            for (var stack : stacks) {
                entries.put(new StackId(stack.type(), stack.id(), stack.nbt()), stack.amount());
            }
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return stack.amount() > 0 &&
                (acceptedIds.isEmpty() || acceptedIds.contains(stack.id())) && total() < capacity;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            if (!acceptInput(stack)) {
                return stack;
            }
            var inserted = Math.min(stack.amount(), capacity - total());
            if (!simulate) {
                var key = key(stack);
                entries.put(key, entries.getOrDefault(key, 0) + inserted);
            }
            return TestStack.ADAPTER.withAmount(stack, stack.amount() - inserted);
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            var key = key(stack);
            var stored = entries.getOrDefault(key, 0);
            var extracted = Math.min(stack.amount(), stored);
            if (extracted <= 0) {
                return TestStack.ADAPTER.empty();
            }
            if (!simulate) {
                if (extracted == stored) {
                    entries.remove(key);
                } else {
                    entries.put(key, stored - extracted);
                }
            }
            return TestStack.ADAPTER.withAmount(stack, extracted);
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            if (limit <= 0) {
                return TestStack.ADAPTER.empty();
            }
            return entries.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .findFirst()
                .map(entry -> extract(stack(entry.getKey(), Math.min(limit, entry.getValue())), simulate))
                .orElseGet(TestStack.ADAPTER::empty);
        }

        @Override
        public long getStorageAmount(TestStack stack) {
            return entries.getOrDefault(key(stack), 0);
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            snapshotCalls++;
            return entries.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> stack(entry.getKey(), entry.getValue()))
                .toList();
        }

        @Override
        public boolean acceptOutput() {
            return !entries.isEmpty();
        }

        private int total() {
            return entries.values().stream().mapToInt(Integer::intValue).sum();
        }

        private int amount(String id) {
            return entries.entrySet().stream()
                .filter(entry -> entry.getKey().id().equals(id))
                .mapToInt(Map.Entry::getValue)
                .sum();
        }

        private List<String> ids() {
            return entries.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey().id())
                .toList();
        }

        private int snapshotCalls() {
            return snapshotCalls;
        }

        private static StackId key(TestStack stack) {
            return new StackId(stack.type(), stack.id(), stack.nbt());
        }

        private static TestStack stack(StackId key, int amount) {
            return new TestStack(key.type(), key.id(), key.nbt(), amount);
        }
    }
}
