package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.logistics.IStackKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestInventoryView implements IInventoryView {
    private final Map<IStackKey, Long> amounts;
    private final Map<IStackKey, Integer> reads = new LinkedHashMap<>();

    private TestInventoryView(Map<IStackKey, Long> amounts) {
        this.amounts = new LinkedHashMap<>(amounts);
    }

    public static TestInventoryView empty() {
        return new TestInventoryView(Map.of());
    }

    public static TestInventoryView fromAmounts(List<CraftAmount> amounts) {
        var snapshot = new LinkedHashMap<IStackKey, Long>();
        for (var amount : amounts) {
            snapshot.put(amount.key(), snapshot.getOrDefault(amount.key(), 0L) + amount.amount());
        }
        return new TestInventoryView(snapshot);
    }

    public int readCount(IStackKey key) {
        return reads.getOrDefault(key, 0);
    }

    @Override
    public long amountOf(IStackKey key) {
        reads.put(key, reads.getOrDefault(key, 0) + 1);
        return amounts.getOrDefault(key, 0L);
    }

    @Override
    public long extract(IStackKey key, long amount, boolean simulate) {
        return 0L;
    }

    @Override
    public long insert(IStackKey key, long amount, boolean simulate) {
        return 0L;
    }
}
