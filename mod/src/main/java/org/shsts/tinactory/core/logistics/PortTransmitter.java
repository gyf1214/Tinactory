package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackAdapter;

import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PortTransmitter<T> {
    private final IStackAdapter<T> stackAdapter;

    public PortTransmitter(IStackAdapter<T> stackAdapter) {
        this.stackAdapter = stackAdapter;
    }

    private T probe(IPort<T> from, IPort<T> to, T stack, int limit, boolean amountAware) {
        var amount = amountAware ? Math.min(stackAdapter.amount(stack), limit) : limit;
        var expected = stackAdapter.withAmount(stack, amount);
        var extracted = from.extract(expected, true);
        var remaining = to.insert(extracted, true);
        var moved = stackAdapter.amount(extracted) - stackAdapter.amount(remaining);
        return moved > 0 ? stackAdapter.withAmount(extracted, moved) : stackAdapter.empty();
    }

    public int transmit(IPort<T> from, IPort<T> to, Predicate<T> filter, int limit) {
        if (limit <= 0) {
            return 0;
        }
        var moved = 0;
        var candidates = from.getAllStorages().stream().filter(filter).toList();
        for (var stack : candidates) {
            if (moved >= limit) {
                break;
            }
            var transferable = probe(from, to, stack, limit - moved, true);
            if (stackAdapter.isEmpty(transferable)) {
                continue;
            }
            moved += transmitCandidate(from, to, transferable);
        }
        return moved;
    }

    public int transmitIdentity(IPort<T> from, IPort<T> to, T identity, int limit) {
        if (limit <= 0) {
            return 0;
        }
        var transferable = probe(from, to, identity, limit, false);
        return stackAdapter.isEmpty(transferable) ? 0 : transmitCandidate(from, to, transferable);
    }

    private int transmitCandidate(IPort<T> from, IPort<T> to, T stack) {
        var extracted = from.extract(stack, false);
        if (stackAdapter.isEmpty(extracted)) {
            return 0;
        }
        var remaining = to.insert(extracted, false);
        return stackAdapter.amount(extracted) - stackAdapter.amount(remaining);
    }

}
