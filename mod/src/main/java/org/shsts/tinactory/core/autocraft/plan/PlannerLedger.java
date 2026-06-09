package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.LinkedHashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PlannerLedger {
    private final Map<IStackKey, Long> inventoryStock = new LinkedHashMap<>();
    private final Map<IStackKey, Long> craftedStock = new LinkedHashMap<>();
    private final Map<IStackKey, PlanSummary.Entry> summary = new LinkedHashMap<>();

    public PlannerLedger copy() {
        var copy = new PlannerLedger();
        copy.inventoryStock.putAll(inventoryStock);
        copy.craftedStock.putAll(craftedStock);
        copy.summary.putAll(summary);
        return copy;
    }

    public void reset(PlannerLedger snapshot) {
        inventoryStock.clear();
        inventoryStock.putAll(snapshot.inventoryStock);
        craftedStock.clear();
        craftedStock.putAll(snapshot.craftedStock);
        summary.clear();
        summary.putAll(snapshot.summary);
    }

    public void observeInventory(IStackKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        inventoryStock.merge(key, amount, Long::sum);
        var entry = entryOf(key);
        summary.put(key, new PlanSummary.Entry(
            entry.existingAmount() + amount,
            entry.consumedFromInventory(),
            entry.craftedAmount()));
    }

    public void recordCraftedAmount(IStackKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        var entry = entryOf(key);
        summary.put(key, new PlanSummary.Entry(
            entry.existingAmount(),
            entry.consumedFromInventory(),
            entry.craftedAmount() + amount));
    }

    public void addCraftedStock(IStackKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        craftedStock.merge(key, amount, Long::sum);
    }

    public void recordUnsatisfiedInventoryDemand(IStackKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        var entry = entryOf(key);
        summary.put(key, new PlanSummary.Entry(
            entry.existingAmount(),
            entry.consumedFromInventory() + amount,
            entry.craftedAmount()));
    }

    public long consume(IStackKey key, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        var inventoryConsumed = consumeFrom(inventoryStock, key, amount);
        if (inventoryConsumed > 0L) {
            var entry = entryOf(key);
            summary.put(key, new PlanSummary.Entry(
                entry.existingAmount(),
                entry.consumedFromInventory() + inventoryConsumed,
                entry.craftedAmount()));
        }
        var remaining = amount - inventoryConsumed;
        return inventoryConsumed + consumeFrom(craftedStock, key, remaining);
    }

    public long consumeCrafted(IStackKey key, long amount) {
        return consumeFrom(craftedStock, key, amount);
    }

    public PlanSummary summary() {
        var entries = new LinkedHashMap<IStackKey, PlanSummary.Entry>();
        for (var item : summary.entrySet()) {
            var entry = item.getValue();
            if (entry.existingAmount() != 0L ||
                entry.consumedFromInventory() != 0L ||
                entry.craftedAmount() != 0L) {
                entries.put(item.getKey(), entry);
            }
        }
        return new PlanSummary(entries);
    }

    private PlanSummary.Entry entryOf(IStackKey key) {
        return summary.getOrDefault(key, new PlanSummary.Entry(0L, 0L, 0L));
    }

    private static long consumeFrom(Map<IStackKey, Long> stock, IStackKey key, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        var available = stock.getOrDefault(key, 0L);
        var consumed = Math.min(available, amount);
        if (consumed <= 0L) {
            return 0L;
        }
        stock.put(key, available - consumed);
        return consumed;
    }
}
