package org.shsts.tinactory.unit.logistics;

import org.shsts.tinactory.api.logistics.PortType;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
record TestStack(String id, int amount) {
    static final IStackAdapter<TestStack> ADAPTER = new IStackAdapter<>() {
        @Override
        public TestStack empty() {
            return new TestStack("", 0);
        }

        @Override
        public boolean isEmpty(TestStack stack) {
            return stack.amount() <= 0;
        }

        @Override
        public TestStack copy(TestStack stack) {
            return new TestStack(stack.id(), stack.amount());
        }

        @Override
        public int amount(TestStack stack) {
            return stack.amount();
        }

        @Override
        public TestStack withAmount(TestStack stack, int amount) {
            return new TestStack(stack.id(), amount);
        }

        @Override
        public boolean canStack(TestStack left, TestStack right) {
            return Objects.equals(left.id(), right.id());
        }

        @Override
        public IIngredientKey keyOf(TestStack stack) {
            return new Key(stack.id());
        }

        @Override
        public TestStack stackOf(IIngredientKey key, long amount) {
            var typed = (Key) key;
            return new TestStack(typed.id(), (int) amount);
        }
    };

    private record Key(String id) implements IIngredientKey {
        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public int compareTo(IIngredientKey other) {
            var typed = (Key) other;
            return id.compareTo(typed.id());
        }
    }
}
