package org.shsts.tinactory.unit.recipe;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackResultTest {
    @Test
    void shouldCreateDeterministicScaledPreview() {
        var result = new StackResult<>("test_stack_result", PortType.FLUID, 0.25d,
            new TestStack(PortType.FLUID, "steam", "hot", 1000), TestStack.ADAPTER);

        var preview = assertInstanceOf(StackResult.class, result.scaledPreview(3));

        assertEquals(new StackResult<>("test_stack_result", PortType.FLUID, 1d,
            new TestStack(PortType.FLUID, "steam", "hot", 3000), TestStack.ADAPTER), preview);
    }

    @Test
    void shouldInsertScaledExactStackIntoMatchingPort() {
        var port = new TestPort(PortType.ITEM, TestStack.item("ingot", 1), 16);
        var result = new StackResult<>("test_stack_result", PortType.ITEM, 1d,
            TestStack.item("ingot", 3), TestStack.ADAPTER);

        var inserted = result.insertPort(port, 2, new Random(1L), false);

        assertTrue(inserted.isPresent());
        assertEquals(new TestStack(PortType.ITEM, "ingot", "", 6),
            assertInstanceOf(StackResult.class, inserted.orElseThrow()).stack());
        assertEquals(TestStack.item("ingot", 7), port.storedStack());
    }

    @Test
    void shouldReturnEmptyWhenChanceProducesNoOutput() {
        var port = new TestPort(PortType.ITEM, TestStack.item("ingot", 1), 16);
        var result = new StackResult<>("test_stack_result", PortType.ITEM, 0d,
            TestStack.item("ingot", 3), TestStack.ADAPTER);

        var inserted = result.insertPort(port, 4, new Random(1L), false);

        assertTrue(inserted.isEmpty());
        assertEquals(TestStack.item("ingot", 1), port.storedStack());
    }

    @Test
    void shouldExposeAdapterDrivenFilter() {
        var result = new StackResult<>("test_stack_result", PortType.FLUID, 1d,
            new TestStack(PortType.FLUID, "steam", "hot", 1000), TestStack.ADAPTER);

        @SuppressWarnings("unchecked")
        var filter = (java.util.function.Predicate<TestStack>) result.filter();

        assertTrue(filter.test(new TestStack(PortType.FLUID, "steam", "hot", 250)));
        assertFalse(filter.test(new TestStack(PortType.FLUID, "steam", "", 250)));
        assertFalse(filter.test(TestStack.item("steam", 1)));
    }
}
