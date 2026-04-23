package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;

public record TestStack(PortType type, String id, String nbt, int amount) {
    public static final Codec<TestStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.xmap(PortType::valueOf, PortType::name).fieldOf("type").forGetter(TestStack::type),
        Codec.STRING.fieldOf("id").forGetter(TestStack::id),
        Codec.STRING.fieldOf("nbt").forGetter(TestStack::nbt),
        Codec.INT.fieldOf("amount").forGetter(TestStack::amount)
    ).apply(instance, TestStack::new));

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
        public IStackKey keyOf(TestStack stack) {
            return new TestStackKey(stack.type(), stack.id(), stack.nbt());
        }

        @Override
        public TestStack stackOf(IStackKey key, long amount) {
            var typed = (TestStackKey) key;
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
