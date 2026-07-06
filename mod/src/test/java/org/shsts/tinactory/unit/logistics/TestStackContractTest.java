package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.unit.fixture.TestStack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TestStackContractTest {
    @Test
    void keyShouldExposeAdapter() {
        var key = TestStack.ADAPTER.keyOf(new TestStack("copper", 1));

        assertSame(TestStack.ADAPTER, key.adapter());
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
