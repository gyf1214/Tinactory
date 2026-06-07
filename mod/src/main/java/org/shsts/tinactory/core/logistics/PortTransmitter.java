package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackAdapter;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PortTransmitter<T> {
    private final IStackAdapter<T> stackAdapter;

    public PortTransmitter(IStackAdapter<T> stackAdapter) {
        this.stackAdapter = stackAdapter;
    }

    public T probe(IPort<T> from, IPort<T> to, T stack, int limit) {
        return probe(from, to, stack, limit, true);
    }

    public T probeIdentity(IPort<T> from, IPort<T> to, T stack, int limit) {
        return probe(from, to, stack, limit, false);
    }

    private T probe(IPort<T> from, IPort<T> to, T stack, int limit, boolean amountAware) {
        var amount = amountAware ? Math.min(stackAdapter.amount(stack), limit) : limit;
        var expected = stackAdapter.withAmount(stack, amount);
        var extracted = from.extract(expected, true);
        var remaining = to.insert(extracted, true);
        var moved = stackAdapter.amount(extracted) - stackAdapter.amount(remaining);
        return moved > 0 ? stackAdapter.withAmount(extracted, moved) : stackAdapter.empty();
    }

    public T select(IPort<T> from, IPort<T> to, Iterable<T> candidates, int limit) {
        for (var stack : candidates) {
            var moved = probe(from, to, stack, limit);
            if (!stackAdapter.isEmpty(moved)) {
                return moved;
            }
        }
        return stackAdapter.empty();
    }

    public T transmit(IPort<T> from, IPort<T> to, T stack) {
        if (stackAdapter.isEmpty(stack)) {
            return stackAdapter.empty();
        }
        var extracted = from.extract(stack, false);
        return to.insert(extracted, false);
    }
}
