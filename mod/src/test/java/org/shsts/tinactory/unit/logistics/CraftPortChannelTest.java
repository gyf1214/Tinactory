package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.logistics.CraftPortChannel;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CraftPortChannelTest {
    @Test
    void extractShouldSupportAmountsLargerThanIntegerMaxValue() {
        var port = new RecordingPort();
        var channel = new CraftPortChannel<>(
            TestStack.ADAPTER,
            port,
            (key, amount) -> new TestStack(key.id(), amount),
            stack -> CraftKey.item(stack.id(), ""));
        var key = CraftKey.item("iron", "");
        var amount = (long) Integer.MAX_VALUE + 19L;

        var extracted = channel.extract(key, amount, false);

        assertEquals(amount, extracted);
        assertEquals(2, port.extractCalls);
    }

    @Test
    void insertShouldSupportAmountsLargerThanIntegerMaxValue() {
        var port = new RecordingPort();
        var channel = new CraftPortChannel<>(
            TestStack.ADAPTER,
            port,
            (key, amount) -> new TestStack(key.id(), amount),
            stack -> CraftKey.item(stack.id(), ""));
        var key = CraftKey.item("iron", "");
        var amount = (long) Integer.MAX_VALUE + 5L;

        var inserted = channel.insert(key, amount, false);

        assertEquals(amount, inserted);
        assertEquals(2, port.insertCalls);
    }

    @Test
    void snapshotShouldMapPortStorageWithCraftKeys() {
        var port = new TestPort("iron", 100, 7);
        var channel = new CraftPortChannel<>(
            TestStack.ADAPTER,
            port,
            (key, amount) -> new TestStack(key.id(), amount),
            stack -> CraftKey.item(stack.id(), ""));

        var snapshot = channel.snapshot();

        assertEquals(List.of(new CraftAmount(CraftKey.item("iron", ""), 7)), snapshot);
    }

    private static final class RecordingPort implements IPort<TestStack> {
        private int extractCalls;
        private int insertCalls;

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return true;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            insertCalls++;
            return TestStack.ADAPTER.empty();
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            extractCalls++;
            return stack;
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            return TestStack.ADAPTER.empty();
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
            return true;
        }
    }
}
