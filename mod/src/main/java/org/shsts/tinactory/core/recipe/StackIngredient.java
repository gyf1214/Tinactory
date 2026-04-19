package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StackIngredient<T> implements IProcessingIngredient {
    private final String codecName;
    private final PortType type;
    private final T stack;
    private final IStackAdapter<T> adapter;

    public StackIngredient(String codecName, PortType type, T stack, IStackAdapter<T> adapter) {
        this.codecName = codecName;
        this.type = type;
        this.stack = stack;
        this.adapter = adapter;
    }

    @Override
    public String codecName() {
        return codecName;
    }

    @Override
    public PortType type() {
        return type;
    }

    public T stack() {
        return stack;
    }

    protected IStackAdapter<T> adapter() {
        return adapter;
    }

    @Override
    public Predicate<?> filter() {
        return (Predicate<T>) stack1 -> adapter.canStack(stack1, stack);
    }

    @Override
    public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
        if (port.type() != type) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        var port1 = (IPort<T>) port;
        var expected = adapter.withAmount(stack, adapter.amount(stack) * parallel);
        var extracted = port1.extract(expected, simulate);
        return adapter.amount(extracted) >= adapter.amount(expected) ?
            Optional.of(new StackIngredient<>(codecName, type, extracted, adapter)) : Optional.empty();
    }

    public static <T> Codec<StackIngredient<T>> codec(String codecName, PortType type,
        Codec<T> stackCodec, IStackAdapter<T> adapter) {
        return stackCodec.xmap(
            stack -> new StackIngredient<>(codecName, type, stack, adapter),
            StackIngredient::stack
        );
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof StackIngredient<?> ingredient &&
            codecName.equals(ingredient.codecName) &&
            type == ingredient.type &&
            Objects.equals(stack, ingredient.stack));
    }

    @Override
    public int hashCode() {
        return Objects.hash(codecName, type, stack);
    }
}
