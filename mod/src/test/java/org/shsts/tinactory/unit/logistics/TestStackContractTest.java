package org.shsts.tinactory.unit.logistics;

import org.shsts.tinactory.unit.fixture.TestStack;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestStackContractTest {
    @Test
    void keyShouldExposePortTypeAndBeComparable() {
        var left = TestStack.ADAPTER.keyOf(new TestStack("copper", 1));
        var right = TestStack.ADAPTER.keyOf(new TestStack("iron", 1));

        assertEquals(PortType.ITEM, left.type());
        assertTrue(left.compareTo(right) < 0);
    }

    @Test
    void adapterShouldRoundTripViaKeyAndLongAmount() {
        var original = new TestStack("iron", 3);
        var key = TestStack.ADAPTER.keyOf(original);

        var remapped = TestStack.ADAPTER.stackOf(key, 9L);

        assertEquals("iron", remapped.id());
        assertEquals(9, remapped.amount());
    }
}
