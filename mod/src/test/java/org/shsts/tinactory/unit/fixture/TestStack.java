package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;

public record TestStack(PortType type, String id, String nbt, int amount) {
    public static final IStackAdapter<TestStack> ADAPTER = new IStackAdapter<>() {
        @Override
        public TestStack empty() {
            return new TestStack(PortType.ITEM, "", "", 0);
        }

        @Override
        public boolean isEmpty(TestStack stack) {
            return stack.amount() <= 0;
        }

        @Override
        public TestStack copy(TestStack stack) {
            return new TestStack(stack.type(), stack.id(), stack.nbt(), stack.amount());
        }

        @Override
        public int amount(TestStack stack) {
            return stack.amount();
        }

        @Override
        public TestStack withAmount(TestStack stack, int amount) {
            return new TestStack(stack.type(), stack.id(), stack.nbt(), amount);
        }

        @Override
        public boolean canStack(TestStack left, TestStack right) {
            return left.type() == right.type() &&
                Objects.equals(left.id(), right.id()) &&
                Objects.equals(left.nbt(), right.nbt());
        }

        @Override
        public IIngredientKey keyOf(TestStack stack) {
            return new TestIngredientKey(stack.type(), stack.id(), stack.nbt());
        }

        @Override
        public TestStack stackOf(IIngredientKey key, long amount) {
            var typed = (TestIngredientKey) key;
            return new TestStack(typed.type(), typed.id(), typed.nbt(), (int) amount);
        }
    };

    public TestStack(String id, int amount) {
        this(PortType.ITEM, id, "", amount);
    }

    public static TestStack item(String id, int amount) {
        return new TestStack(PortType.ITEM, id, "", amount);
    }

    public static TestStack fluid(String id, int amount) {
        return new TestStack(PortType.FLUID, id, "", amount);
    }
}
