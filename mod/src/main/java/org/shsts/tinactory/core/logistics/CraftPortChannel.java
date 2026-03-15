package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CraftPortChannel<T> {
    private final IStackAdapter<T> stackAdapter;
    private final IPort<T> port;

    public CraftPortChannel(IStackAdapter<T> stackAdapter, IPort<T> port) {
        this.stackAdapter = stackAdapter;
        this.port = port;
    }

    public long amountOf(IIngredientKey key) {
        return port.getStorageAmount(stackAdapter.stackOf(key, 1L));
    }

    public long extract(IIngredientKey key, long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        var left = amount;
        long extractedTotal = 0L;
        while (left > 0L) {
            var chunk = (int) Math.min(left, Integer.MAX_VALUE);
            var expected = stackAdapter.stackOf(key, chunk);
            var moved = stackAdapter.amount(port.extract(expected, simulate));
            if (moved <= 0L) {
                break;
            }
            extractedTotal += moved;
            left -= moved;
        }
        return extractedTotal;
    }

    public long insert(IIngredientKey key, long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        var left = amount;
        long insertedTotal = 0L;
        while (left > 0L) {
            var chunk = (int) Math.min(left, Integer.MAX_VALUE);
            var expected = stackAdapter.stackOf(key, chunk);
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
                ret.add(new CraftAmount(stackAdapter.keyOf(stack), stackAdapter.amount(stack)));
            }
        }
        return ret;
    }
}
