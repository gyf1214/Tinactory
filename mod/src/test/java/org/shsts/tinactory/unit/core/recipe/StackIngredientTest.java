package org.shsts.tinactory.unit.core.recipe;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackIngredientTest {
    @Test
    void shouldConsumeScaledExactStackFromMatchingPort() {
        var port = new TestStackPort(PortType.ITEM, TestStack.item("ore", 8), 16);
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
            TestStack.item("ore", 2), TestStack.ADAPTER);

        var consumed = ingredient.consumePort(port, 3, false);

        assertTrue(consumed.isPresent());
        assertEquals(new TestStack(PortType.ITEM, "ore", "", 6),
            assertInstanceOf(StackIngredient.class, consumed.orElseThrow()).stack());
        assertEquals(2, port.stored().amount());
    }

    @Test
    void shouldNotConsumeWhenPortTypeDoesNotMatch() {
        var port = new TestStackPort(PortType.FLUID, TestStack.fluid("water", 8), 16);
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
            TestStack.item("ore", 2), TestStack.ADAPTER);

        assertTrue(ingredient.consumePort(port, 1, false).isEmpty());
        assertEquals(TestStack.fluid("water", 8), port.stored());
    }

    @Test
    void shouldExposeAdapterDrivenFilter() {
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
            new TestStack(PortType.ITEM, "ore", "nbt", 2), TestStack.ADAPTER);

        @SuppressWarnings("unchecked")
        var filter = (java.util.function.Predicate<TestStack>) ingredient.filter();

        assertTrue(filter.test(new TestStack(PortType.ITEM, "ore", "nbt", 1)));
        assertFalse(filter.test(new TestStack(PortType.ITEM, "ore", "", 1)));
        assertFalse(filter.test(TestStack.fluid("ore", 1)));
    }

    private static final class TestStackPort implements IPort<TestStack> {
        private final PortType type;
        private final int capacity;
        private TestStack stored;

        private TestStackPort(PortType type, TestStack stored, int capacity) {
            this.type = type;
            this.stored = stored;
            this.capacity = capacity;
        }

        @Override
        public PortType type() {
            return type;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return type == stack.type() &&
                TestStack.ADAPTER.canStack(stored, stack) &&
                stored.amount() < capacity;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            if (!acceptInput(stack)) {
                return stack;
            }
            var inserted = Math.min(stack.amount(), capacity - stored.amount());
            if (!simulate) {
                stored = TestStack.ADAPTER.withAmount(stored, stored.amount() + inserted);
            }
            return TestStack.ADAPTER.withAmount(stack, stack.amount() - inserted);
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            if (type != stack.type() || !TestStack.ADAPTER.canStack(stored, stack) || stored.amount() <= 0) {
                return TestStack.ADAPTER.empty();
            }
            var moved = Math.min(stack.amount(), stored.amount());
            if (!simulate) {
                stored = TestStack.ADAPTER.withAmount(stored, stored.amount() - moved);
            }
            return TestStack.ADAPTER.withAmount(stack, moved);
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(TestStack stack) {
            return type == stack.type() && TestStack.ADAPTER.canStack(stored, stack) ? stored.amount() : 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return stored.amount() > 0 ? List.of(stored) : List.of();
        }

        @Override
        public boolean acceptOutput() {
            return stored.amount() > 0;
        }

        private TestStack stored() {
            return stored;
        }
    }
}
