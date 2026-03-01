package org.shsts.tinactory.unit.logistics;

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
    };

    private record Key(String id) implements IIngredientKey {}
}
