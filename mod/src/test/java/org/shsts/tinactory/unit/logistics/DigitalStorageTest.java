package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.DigitalStorage;
import org.shsts.tinactory.core.logistics.IDigitalProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DigitalStorageTest {
    private static class TestStorage extends DigitalStorage<TestStack> {
        public TestStorage(IDigitalProvider provider, int bytesPerType, int bytesPerUnit) {
            super(provider, TestStack.ADAPTER, bytesPerType, bytesPerUnit);
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }
    }

    @Test
    void shouldTrackBytesAndRemainderForInsertAndExtract() {
        var provider = new FakeDigitalProvider(20);
        var storage = new TestStorage(provider, 4, 2);

        var firstRemainder = storage.insert(new TestStack("iron", 5), false);
        var secondRemainder = storage.insert(new TestStack("iron", 5), false);

        assertEquals(0, firstRemainder.amount());
        assertEquals(2, secondRemainder.amount());
        assertEquals(8, storage.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(20, provider.bytesUsed());

        var extracted = storage.extract(new TestStack("iron", 3), false);

        assertEquals(3, extracted.amount());
        assertEquals(5, storage.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(14, provider.bytesUsed());
    }

    @Test
    void shouldHonorFilters() {
        var provider = new FakeDigitalProvider(20);
        var storage = new TestStorage(provider, 2, 1);
        storage.setFilters(List.of(stack -> stack.id().equals("gold")));

        var blocked = storage.insert(new TestStack("iron", 1), false);
        var accepted = storage.insert(new TestStack("gold", 1), false);

        assertEquals(1, blocked.amount());
        assertEquals(0, accepted.amount());
        storage.resetFilters();
        assertEquals(0, storage.insert(new TestStack("iron", 1), false).amount());
    }

    @Test
    void shouldNotMutateStateWhenSimulating() {
        var provider = new FakeDigitalProvider(20);
        var storage = new TestStorage(provider, 2, 1);
        storage.insert(new TestStack("iron", 3), false);

        var insertRemainder = storage.insert(new TestStack("iron", 10), true);
        var extracted = storage.extract(new TestStack("iron", 2), true);

        assertTrue(insertRemainder.amount() >= 0);
        assertEquals(2, extracted.amount());
        assertEquals(3, storage.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(5, provider.bytesUsed());
    }

    @Test
    void extractAnyShouldDrainFirstEntryWithinLimit() {
        var provider = new FakeDigitalProvider(100);
        var storage = new TestStorage(provider, 3, 1);
        storage.insert(new TestStack("iron", 4), false);

        var extracted = storage.extract(2, false);

        assertFalse(extracted.id().isEmpty());
        assertEquals(2, extracted.amount());
        assertEquals(2, storage.getStorageAmount(new TestStack("iron", 1)));
    }

    private static final class FakeDigitalProvider implements IDigitalProvider {
        private final int capacity;
        private int used;

        private FakeDigitalProvider(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public int bytesCapacity() {
            return capacity;
        }

        @Override
        public int bytesUsed() {
            return used;
        }

        @Override
        public int consumeLimit(int offset, int bytes) {
            return Math.max(0, (capacity - used - offset) / bytes);
        }

        @Override
        public void consume(int bytes) {
            used += bytes;
        }

        @Override
        public void reset() {
            used = 0;
        }
    }
}
