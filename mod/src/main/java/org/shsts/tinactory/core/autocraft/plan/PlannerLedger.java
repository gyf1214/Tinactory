package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PlannerLedger {
    private final Map<IIngredientKey, Long> stock = new HashMap<>();

    public PlannerLedger copy() {
        var copy = new PlannerLedger();
        copy.stock.putAll(stock);
        return copy;
    }

    public void reset(PlannerLedger snapshot) {
        stock.clear();
        stock.putAll(snapshot.stock);
    }

    public long get(IIngredientKey key) {
        return stock.getOrDefault(key, 0L);
    }

    public void add(IIngredientKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        stock.merge(key, amount, Long::sum);
    }

    public long consume(IIngredientKey key, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        var available = get(key);
        var consumed = Math.min(available, amount);
        if (consumed > 0L) {
            stock.put(key, available - consumed);
        }
        return consumed;
    }
}
