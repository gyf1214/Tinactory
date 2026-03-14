package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CraftPortChannel<T> {
    private final IStackAdapter<T> stackAdapter;
    private final IPort<T> port;
    private final BiFunction<CraftKey, Integer, T> stackOf;
    private final Function<T, CraftKey> keyOf;

    public CraftPortChannel(
        IStackAdapter<T> stackAdapter,
        IPort<T> port,
        BiFunction<CraftKey, Integer, T> stackOf,
        Function<T, CraftKey> keyOf) {
        this.stackAdapter = stackAdapter;
        this.port = port;
        this.stackOf = stackOf;
        this.keyOf = keyOf;
    }

    public long amountOf(CraftKey key) {
        return port.getStorageAmount(stackOf.apply(key, 1));
    }

    public long extract(CraftKey key, long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        var left = amount;
        long extractedTotal = 0L;
        while (left > 0L) {
            var chunk = (int) Math.min(left, Integer.MAX_VALUE);
            var expected = stackOf.apply(key, chunk);
            var moved = stackAdapter.amount(port.extract(expected, simulate));
            if (moved <= 0L) {
                break;
            }
            extractedTotal += moved;
            left -= moved;
        }
        return extractedTotal;
    }

    public long insert(CraftKey key, long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        var left = amount;
        long insertedTotal = 0L;
        while (left > 0L) {
            var chunk = (int) Math.min(left, Integer.MAX_VALUE);
            var expected = stackOf.apply(key, chunk);
            var remaining = port.insert(expected, simulate);
            var moved = chunk - stackAdapter.amount(remaining);
            if (moved <= 0L) {
                break;
            }
            insertedTotal += moved;
            left -= moved;
        }
        return insertedTotal;
    }

    public List<CraftAmount> snapshot() {
        var ret = new ArrayList<CraftAmount>();
        for (var stack : port.getAllStorages()) {
            if (!stackAdapter.isEmpty(stack)) {
                ret.add(new CraftAmount(keyOf.apply(stack), stackAdapter.amount(stack)));
            }
        }
        return ret;
    }
}
