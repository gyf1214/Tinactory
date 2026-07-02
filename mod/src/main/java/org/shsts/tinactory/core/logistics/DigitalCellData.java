package org.shsts.tinactory.core.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class DigitalCellData {
    public static final DigitalCellData EMPTY = new DigitalCellData(Map.of());

    private final Map<IStackKey, Long> entries;
    private final int keyCount;
    private final long totalAmount;

    private DigitalCellData(Map<IStackKey, Long> entries) {
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
        keyCount = this.entries.size();
        totalAmount = totalAmount(this.entries);
    }

    public static DigitalCellData of(Map<IStackKey, Long> entries) {
        var filtered = new LinkedHashMap<IStackKey, Long>();
        for (var entry : entries.entrySet()) {
            if (entry.getValue() > 0L) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered.isEmpty() ? EMPTY : new DigitalCellData(filtered);
    }

    public Map<IStackKey, Long> entries() {
        return entries;
    }

    public int keyCount() {
        return keyCount;
    }

    public long totalAmount() {
        return totalAmount;
    }

    public DigitalCellData withEntry(IStackKey key, long amount) {
        var current = entries.getOrDefault(key, 0L);
        if (current == amount || (current == 0L && amount <= 0L)) {
            return this;
        }
        var next = new LinkedHashMap<>(entries);
        if (amount <= 0L) {
            next.remove(key);
        } else {
            next.put(key, amount);
        }
        return next.isEmpty() ? EMPTY : new DigitalCellData(next);
    }

    public static Codec<DigitalCellData> codec(Codec<IStackKey> keyCodec) {
        var entryCodec = RecordCodecBuilder.<Entry>create(instance -> instance.group(
            keyCodec.fieldOf("key").forGetter(Entry::key),
            Codec.LONG.fieldOf("amount").forGetter(Entry::amount)
        ).apply(instance, Entry::new));
        return entryCodec.listOf().xmap(DigitalCellData::fromEntries, DigitalCellData::toEntries);
    }

    private static DigitalCellData fromEntries(List<Entry> entries) {
        var map = new LinkedHashMap<IStackKey, Long>();
        for (var entry : entries) {
            if (entry.amount() > 0L) {
                map.put(entry.key(), entry.amount());
            }
        }
        return of(map);
    }

    private static List<Entry> toEntries(DigitalCellData data) {
        var ret = new ArrayList<Entry>(data.entries.size());
        for (var entry : data.entries.entrySet()) {
            ret.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return ret;
    }

    private static long totalAmount(Map<IStackKey, Long> entries) {
        var ret = 0L;
        for (var amount : entries.values()) {
            ret = saturatedAdd(ret, amount);
        }
        return ret;
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof DigitalCellData data && entries.equals(data.entries));
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    private record Entry(IStackKey key, long amount) {}
}
