package org.shsts.tinactory.unit.logistics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.DigitalStorage;
import org.shsts.tinactory.core.logistics.IDigitalProvider;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.EMPTY_REGISTRY;

class DigitalStorageTest {
    private static class TestStorage extends DigitalStorage<TestStack> {
        public TestStorage(IDigitalProvider provider, int bytesPerType, int bytesPerUnit) {
            super(provider, TestStack.ADAPTER, bytesPerType, bytesPerUnit);
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        protected CompoundTag serializeStack(HolderLookup.Provider provider, TestStack stack) {
            return (CompoundTag) CodecHelper.encodeTag(provider, TestStack.CODEC, stack);
        }

        @Override
        protected TestStack deserializeStack(HolderLookup.Provider provider, CompoundTag tag) {
            return CodecHelper.parseTag(provider, TestStack.CODEC, tag);
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
    void shouldDifferentiateNewTypeInsertFromExistingTypeInsert() {
        var provider = new FakeDigitalProvider(30);
        var storage = new TestStorage(provider, 4, 2);

        var firstRemainder = storage.insert(new TestStack("iron", 3), false);
        var secondRemainder = storage.insert(new TestStack("iron", 3), false);

        assertEquals(0, firstRemainder.amount());
        assertEquals(0, secondRemainder.amount());
        assertEquals(6, storage.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(16, provider.bytesUsed());
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
    void shouldRejectInputWhenProviderCannotAcceptIt() {
        var bytesLimited = new TestStorage(new FakeDigitalProvider(5), 4, 2);

        assertFalse(bytesLimited.acceptInput(new TestStack("iron", 1)));
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
    void shouldRestoreContentsAndProviderUsageFromSerializedEntries() {
        var sourceProvider = new FakeDigitalProvider(20);
        var source = new TestStorage(sourceProvider, 4, 2);
        source.insert(new TestStack("iron", 5), false);

        var restoredProvider = new FakeDigitalProvider(20);
        var restored = new TestStorage(restoredProvider, 4, 2);
        restored.deserializeFromList(EMPTY_REGISTRY, source.serializeToList(EMPTY_REGISTRY));

        assertEquals(5, restored.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(14, restoredProvider.bytesUsed());
    }

    @Test
    void shouldRestoreMultipleEntriesAndContinueTrackingProviderUsage() {
        var sourceProvider = new FakeDigitalProvider(30);
        var source = new TestStorage(sourceProvider, 4, 2);
        source.insert(new TestStack("iron", 3), false);
        source.insert(new TestStack("gold", 2), false);

        var restoredProvider = new FakeDigitalProvider(30);
        var restored = new TestStorage(restoredProvider, 4, 2);
        restored.deserializeFromList(EMPTY_REGISTRY, source.serializeToList(EMPTY_REGISTRY));

        assertEquals(3, restored.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(2, restored.getStorageAmount(new TestStack("gold", 1)));
        assertEquals(18, restoredProvider.bytesUsed());

        assertEquals(0, restored.insert(new TestStack("iron", 1), false).amount());
        assertEquals(20, restoredProvider.bytesUsed());

        var extracted = restored.extract(new TestStack("gold", 2), false);
        assertEquals(2, extracted.amount());
        assertEquals(12, restoredProvider.bytesUsed());
        assertEquals(0, restored.getStorageAmount(new TestStack("gold", 1)));
    }

    @Test
    void shouldIgnoreEmptyDeserializedEntries() {
        var provider = new FakeDigitalProvider(20);
        var storage = new TestStorage(provider, 4, 2);

        storage.deserializeEntry(new TestStack("iron", 0));

        assertTrue(storage.getAllStorages().isEmpty());
        assertEquals(0, provider.bytesUsed());
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

    @Test
    void extractAnyShouldHandleZeroEmptyAndFullDrainBranches() {
        var provider = new FakeDigitalProvider(100);
        var storage = new TestStorage(provider, 3, 1);

        assertTrue(storage.extract(0, false).amount() <= 0);
        assertTrue(storage.extract(2, false).amount() <= 0);

        storage.insert(new TestStack("iron", 4), false);
        var drained = storage.extract(4, false);

        assertEquals(4, drained.amount());
        assertEquals(0, storage.getStorageAmount(new TestStack("iron", 1)));
        assertEquals(0, provider.bytesUsed());
    }

    private static final class FakeDigitalProvider implements IDigitalProvider {
        private final int capacity;
        private int used;

        private FakeDigitalProvider(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public long bytesCapacity() {
            return capacity;
        }

        @Override
        public long bytesUsed() {
            return used;
        }

        @Override
        public int consumeLimit(int offset, int bytes) {
            return Math.max(0, (capacity - used - offset) / bytes);
        }

        @Override
        public void consume(long bytes) {
            used += Math.toIntExact(bytes);
        }

        @Override
        public void reset() {
            used = 0;
        }
    }
}
