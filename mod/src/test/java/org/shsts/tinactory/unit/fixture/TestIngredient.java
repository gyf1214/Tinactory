package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;

import java.util.Optional;

public final class TestIngredient extends TestProcessingObject implements IProcessingIngredient {
    public static final Codec<TestIngredient> CODEC = Codec.STRING.xmap(
        value -> {
            var parts = value.split(":");
            return new TestIngredient(parts[0], Integer.parseInt(parts[1]));
        },
        value -> value.key() + ":" + value.amount());

    public TestIngredient(String key, int amount) {
        super(key, amount);
    }

    @Override
    public String codecName() {
        return "test_ingredient";
    }

    @Override
    public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        var port1 = (IPort<TestStack>) port;
        var expected = TestStack.item(key(), amount() * parallel);
        var extracted = port1.extract(expected, simulate);
        return extracted.amount() >= expected.amount() ?
            Optional.of(new TestIngredient(key(), extracted.amount())) : Optional.empty();
    }
}
