package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ChannelMachineRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChannelMachineRouteTest {
    @Test
    void inputRouteShouldTransferViaInsert() {
        var key = TestStack.ADAPTER.keyOf(new TestStack("tinactory:iron", 1));
        var port = new RecordingPort();
        var route = new ChannelMachineRoute<>(
            key,
            IMachineRoute.Direction.INPUT,
            TestStack.ADAPTER,
            port);

        var moved = route.transfer(17, false);

        assertEquals(17L, moved);
        assertEquals(1, port.insertCalls);
        assertEquals(0, port.extractCalls);
    }

    @Test
    void outputRouteShouldTransferViaExtract() {
        var key = TestStack.ADAPTER.keyOf(new TestStack("tinactory:iron", 1));
        var port = new RecordingPort();
        var route = new ChannelMachineRoute<>(
            key,
            IMachineRoute.Direction.OUTPUT,
            TestStack.ADAPTER,
            port);

        var moved = route.transfer(17, false);

        assertEquals(17L, moved);
        assertEquals(0, port.insertCalls);
        assertEquals(1, port.extractCalls);
    }

    private static final class RecordingPort implements IPort<TestStack> {
        private int insertCalls;
        private int extractCalls;

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
            return new TestStack(stack.type(), stack.id(), stack.nbt(), 0);
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
