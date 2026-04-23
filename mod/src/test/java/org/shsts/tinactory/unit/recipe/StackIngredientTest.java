package org.shsts.tinactory.unit.recipe;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackIngredientTest {
    @Test
    void shouldConsumeScaledExactStackFromMatchingPort() {
        var port = new TestPort(PortType.ITEM, TestStack.item("ore", 8), 16);
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
            TestStack.item("ore", 2), TestStack.ADAPTER);

        var consumed = ingredient.consumePort(port, 3, false);

        assertTrue(consumed.isPresent());
        assertEquals(new TestStack(PortType.ITEM, "ore", "", 6),
            assertInstanceOf(StackIngredient.class, consumed.orElseThrow()).stack());
        assertEquals(2, port.stored());
    }

    @Test
    void shouldNotConsumeWhenPortTypeDoesNotMatch() {
        var port = new TestPort(PortType.FLUID, TestStack.fluid("water", 8), 16);
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
            TestStack.item("ore", 2), TestStack.ADAPTER);

        assertTrue(ingredient.consumePort(port, 1, false).isEmpty());
        assertEquals(TestStack.fluid("water", 8), port.storedStack());
    }

    @Test
    void shouldExposeAdapterDrivenFilter() {
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
            new TestStack(PortType.ITEM, "ore", "nbt", 2), TestStack.ADAPTER);

        @SuppressWarnings("unchecked")
        var filter = (Predicate<TestStack>) ingredient.filter();

        assertTrue(filter.test(new TestStack(PortType.ITEM, "ore", "nbt", 1)));
        assertFalse(filter.test(new TestStack(PortType.ITEM, "ore", "", 1)));
        assertFalse(filter.test(TestStack.fluid("ore", 1)));
    }
}
