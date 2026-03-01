package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.CombinedPort;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombinedPortTest {
    private static class TestCombinedPort extends CombinedPort<TestStack> {
        public TestCombinedPort(Collection<IPort<TestStack>> composes) {
            super(TestStack.ADAPTER, composes);
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
        assertEquals(3, first.stored);
        assertEquals(5, second.stored);
    }

    @Test
    void shouldAggregateExtractAcrossComposes() {
        var first = new TestPort("iron", 10, 2);
        var second = new TestPort("iron", 10, 4);
        var combined = new TestCombinedPort(List.of(first, second));

        var extracted = combined.extract(new TestStack("iron", 5), false);

        assertEquals("iron", extracted.id());
        assertEquals(5, extracted.amount());
        assertEquals(0, first.stored);
        assertEquals(1, second.stored);
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
        assertEquals(5, port.stored);
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
        assertEquals(5, first.stored);
        assertEquals(1, second.stored);
    }
}
