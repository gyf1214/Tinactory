package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PlannerLedger {
    private final Map<CraftKey, Long> stock = new HashMap<>();

    public long get(CraftKey key) {
        return stock.getOrDefault(key, 0L);
    }

    public void add(CraftKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        stock.merge(key, amount, Long::sum);
    }

    public long consume(CraftKey key, long amount) {
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
