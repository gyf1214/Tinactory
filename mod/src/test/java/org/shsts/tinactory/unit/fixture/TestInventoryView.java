package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestInventoryView implements IInventoryView {
    private final Map<IIngredientKey, Long> amounts;
    private final Map<IIngredientKey, Integer> reads = new LinkedHashMap<>();

    private TestInventoryView(Map<IIngredientKey, Long> amounts) {
        this.amounts = new LinkedHashMap<>(amounts);
    }

    public static TestInventoryView empty() {
        return new TestInventoryView(Map.of());
    }

    public static TestInventoryView fromAmounts(List<CraftAmount> amounts) {
        var snapshot = new LinkedHashMap<IIngredientKey, Long>();
        for (var amount : amounts) {
            snapshot.put(amount.key(), snapshot.getOrDefault(amount.key(), 0L) + amount.amount());
        }
        return new TestInventoryView(snapshot);
    }

    public int readCount(IIngredientKey key) {
        return reads.getOrDefault(key, 0);
    }

    @Override
    public long amountOf(IIngredientKey key) {
        reads.put(key, reads.getOrDefault(key, 0) + 1);
        return amounts.getOrDefault(key, 0L);
    }

    @Override
    public long extract(IIngredientKey key, long amount, boolean simulate) {
        return 0L;
    }

    @Override
    public long insert(IIngredientKey key, long amount, boolean simulate) {
        return 0L;
    }
}
