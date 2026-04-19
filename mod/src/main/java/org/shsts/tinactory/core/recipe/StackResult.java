package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StackResult<T> implements IProcessingResult {
    private final String codecName;
    private final PortType type;
    private final double rate;
    private final T stack;
    private final IStackAdapter<T> adapter;

    public StackResult(String codecName, PortType type, double rate, T stack, IStackAdapter<T> adapter) {
        this.codecName = codecName;
        this.type = type;
        this.rate = rate;
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

    public double rate() {
        return rate;
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
    public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random, boolean simulate) {
        if (port.type() != type) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        var port1 = (IPort<T>) port;
        if (rate < 1d && !simulate) {
            parallel = MathUtil.sampleBinomial(parallel, rate, random);
            if (parallel <= 0) {
                return Optional.empty();
            }
        }
        var stack1 = adapter.withAmount(stack, adapter.amount(stack) * parallel);
        return port1.acceptInput(stack1) && adapter.isEmpty(port1.insert(stack1, simulate)) ?
            Optional.of(new StackResult<>(codecName, type, 1d, stack1, adapter)) : Optional.empty();
    }

    @Override
    public IProcessingResult scaledPreview(int parallel) {
        return new StackResult<>(codecName, type, 1d,
            adapter.withAmount(stack, adapter.amount(stack) * parallel), adapter);
    }

    public static <T> Codec<StackResult<T>> codec(String codecName, PortType type,
        Codec<T> stackCodec, IStackAdapter<T> adapter) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("rate").forGetter(StackResult::rate),
            stackCodec.fieldOf("stack").forGetter(StackResult::stack)
        ).apply(instance, (rate, stack) -> new StackResult<>(codecName, type, rate, stack, adapter)));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof StackResult<?> result &&
            codecName.equals(result.codecName) &&
            type == result.type &&
            Double.compare(rate, result.rate) == 0 &&
            Objects.equals(stack, result.stack));
    }

    @Override
    public int hashCode() {
        return Objects.hash(codecName, type, rate, stack);
    }
}
